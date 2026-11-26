package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Usuario;
import com.ddaii.dto.ProductoDTO;
import com.ddaii.dto.ResultadoVisualDTO;
import com.ddaii.exceptions.AccesoPremiumException;
import com.ddaii.exceptions.SinCoincidenciasException;
import com.ddaii.exceptions.XimilarException;
import com.ddaii.integrations.XimilarClient;
import com.ddaii.integrations.XimilarFacade;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusquedaVisualServiceTest {

    @Mock
    private XimilarFacade ximilarFacade;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoService productoService;

    private BusquedaVisualService service;

    @BeforeEach
    void setUp() {
        // 3 intentos, 5 resultados en el primero, distancia máxima 1.0, imagen hasta 5 MB.
        service = new BusquedaVisualService(
                ximilarFacade, productoRepository, usuarioRepository, productoService,
                3, 5, 1.0, 5L * 1024 * 1024);
    }

    private MockMultipartFile imagen() {
        return new MockMultipartFile("imagen", "foto.jpg", "image/jpeg", "bytes".getBytes(StandardCharsets.UTF_8));
    }

    private void premium(boolean premium) {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setPremium(premium);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
    }

    private Producto producto(Long id) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Producto " + id);
        return p;
    }

    private ProductoDTO dto(Long id) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(id);
        return dto;
    }

    @Test
    void usuarioNoPremiumNoGastaCreditos() {
        premium(false);

        assertThatThrownBy(() -> service.buscar("7", imagen(), 8))
                .isInstanceOf(AccesoPremiumException.class)
                .hasMessageContaining("premium");
        verifyNoInteractions(ximilarFacade);
    }

    @Test
    void sinPrincipalNoGastaCreditos() {
        assertThatThrownBy(() -> service.buscar(null, imagen(), 8))
                .isInstanceOf(AccesoPremiumException.class);
        verifyNoInteractions(usuarioRepository, ximilarFacade);
    }

    @Test
    void devuelveProductosDelCatalogoEnElOrdenDeXimilar() {
        premium(true);
        Producto primero = producto(2L);
        Producto segundo = producto(1L);
        when(ximilarFacade.buscarPorImagen(any(byte[].class), eq("image/jpeg"), eq(5)))
                .thenReturn(List.of(
                        new XimilarClient.Coincidencia(2L, 0.3),
                        new XimilarClient.Coincidencia(1L, 0.5)));
        // findAllById no respeta el orden: el servicio debe reordenar según Ximilar.
        when(productoRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(segundo, primero));
        when(productoService.toDto(primero)).thenReturn(dto(2L));
        when(productoService.toDto(segundo)).thenReturn(dto(1L));

        List<ResultadoVisualDTO> resultado = service.buscar("7", imagen(), 8);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(r -> r.getProducto().getId()).containsExactly(2L, 1L);
        assertThat(resultado.get(0).getDistancia()).isEqualTo(0.3);
        assertThat(resultado.get(1).getDistancia()).isEqualTo(0.5);
        verify(ximilarFacade, times(1)).buscarPorImagen(any(byte[].class), eq("image/jpeg"), eq(5));
    }

    @Test
    void conUnaCoincidenciaValidaCortaEnElPrimerIntento() {
        premium(true);
        when(ximilarFacade.buscarPorImagen(any(byte[].class), eq("image/jpeg"), eq(5)))
                .thenReturn(List.of(new XimilarClient.Coincidencia(9L, 0.4)));
        when(productoRepository.findAllById(List.of(9L))).thenReturn(List.of(producto(9L)));
        when(productoService.toDto(any(Producto.class))).thenReturn(dto(9L));

        assertThat(service.buscar("7", imagen(), 8)).hasSize(1);
        verify(ximilarFacade, times(1)).buscarPorImagen(any(byte[].class), anyString(), anyInt());
    }

    @Test
    void sinCoincidenciasAgotaLosIntentosYDevuelveError() {
        premium(true);
        // Ninguna prenda supera el umbral: se prueba con k cada vez más grande y se corta.
        when(ximilarFacade.buscarPorImagen(any(byte[].class), anyString(), anyInt()))
                .thenReturn(List.of(
                        new XimilarClient.Coincidencia(2L, 1.5),
                        new XimilarClient.Coincidencia(3L, 2.0)));

        assertThatThrownBy(() -> service.buscar("7", imagen(), 8))
                .isInstanceOf(SinCoincidenciasException.class)
                .hasMessageContaining("Se probaron 3 búsquedas");

        ArgumentCaptor<Integer> k = ArgumentCaptor.forClass(Integer.class);
        verify(ximilarFacade, times(3)).buscarPorImagen(any(byte[].class), eq("image/jpeg"), k.capture());
        assertThat(k.getAllValues()).containsExactly(5, 10, 15);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void idsQueNoEstanEnElCatalogoNoCuentanComoCoincidencia() {
        premium(true);
        when(ximilarFacade.buscarPorImagen(any(byte[].class), anyString(), anyInt()))
                .thenReturn(List.of(new XimilarClient.Coincidencia(999L, 0.2)));
        when(productoRepository.findAllById(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.buscar("7", imagen(), 8))
                .isInstanceOf(SinCoincidenciasException.class);
    }

    @Test
    void falloDeXimilarSePropagaComoError502() {
        premium(true);
        when(ximilarFacade.buscarPorImagen(any(byte[].class), anyString(), anyInt()))
                .thenThrow(new XimilarException("Ximilar no responde"));

        assertThatThrownBy(() -> service.buscar("7", imagen(), 8))
                .isInstanceOf(XimilarException.class)
                .hasMessageContaining("Ximilar no responde");
        verify(ximilarFacade, times(1)).buscarPorImagen(any(byte[].class), anyString(), anyInt());
    }

    @Test
    void archivoQueNoEsImagenNoLlamaAXimilar() {
        premium(true);
        MockMultipartFile texto = new MockMultipartFile(
                "imagen", "foto.txt", "text/plain", "no soy imagen".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.buscar("7", texto, 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imagen");
        verify(ximilarFacade, never()).buscarPorImagen(any(), anyString(), anyInt());
    }
}
