package com.ddaii.services;

import com.ddaii.domain.InteresProducto;
import com.ddaii.domain.Producto;
import com.ddaii.domain.Usuario;
import com.ddaii.exceptions.AccesoPremiumException;
import com.ddaii.repositories.InteresProductoRepository;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteresServiceTest {

    @Mock
    private InteresProductoRepository interesRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ProductoRepository productoRepository;

    private InteresService service;

    @BeforeEach
    void setUp() {
        service = new InteresService(interesRepository, usuarioRepository, productoRepository,
                30, 50, 0.15, 0.10);
    }

    private Usuario usuario(boolean premium) {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setPremium(premium);
        return u;
    }

    private Producto producto(String marca, String categoria) {
        Producto p = new Producto();
        p.setId(10L);
        p.setMarca(marca);
        p.setCategoria(categoria);
        return p;
    }

    @Test
    void usuarioNoPremiumNoPuedeRegistrarVista() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> service.registrarVista(7L, 10L))
                .isInstanceOf(AccesoPremiumException.class)
                .hasMessageContaining("premium");
        verifyNoInteractions(interesRepository, productoRepository);
    }

    @Test
    void favoritoDeNoPremiumNoGeneraHistorialPeroSigueSiendoFavorito() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(false)));

        service.registrarFavorito(7L, 10L);

        verify(interesRepository, never()).save(any());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void unaVistaNuevaSeGuardaConPesoUno() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(true)));
        Producto p = producto("Nike", "CALZADO");
        when(productoRepository.findById(10L)).thenReturn(Optional.of(p));
        when(interesRepository.findByUsuarioIdAndProductoIdAndTipo(
                7L, 10L, InteresProducto.Tipo.VISTA)).thenReturn(Optional.empty());

        service.registrarVista(7L, 10L);

        ArgumentCaptor<InteresProducto> guardado = ArgumentCaptor.forClass(InteresProducto.class);
        verify(interesRepository).save(guardado.capture());
        assertThat(guardado.getValue().getTipo()).isEqualTo(InteresProducto.Tipo.VISTA);
        assertThat(guardado.getValue().getPeso()).isEqualTo(1);
    }

    @Test
    void verVariasVecesLaMismaPrendaSumaPeso() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(true)));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto("Nike", "CALZADO")));
        InteresProducto anterior = new InteresProducto(
                usuario(true), producto("Nike", "CALZADO"), InteresProducto.Tipo.VISTA, 2);
        when(interesRepository.findByUsuarioIdAndProductoIdAndTipo(
                7L, 10L, InteresProducto.Tipo.VISTA)).thenReturn(Optional.of(anterior));

        service.registrarVista(7L, 10L);

        assertThat(anterior.getPeso()).isEqualTo(3);
        verify(interesRepository).save(anterior);
    }

    @Test
    void unFavoritoValeTresVistas() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(true)));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto("Nike", "CALZADO")));
        when(interesRepository.findByUsuarioIdAndProductoIdAndTipo(
                7L, 10L, InteresProducto.Tipo.FAVORITO)).thenReturn(Optional.empty());

        service.registrarFavorito(7L, 10L);

        ArgumentCaptor<InteresProducto> guardado = ArgumentCaptor.forClass(InteresProducto.class);
        verify(interesRepository).save(guardado.capture());
        assertThat(guardado.getValue().getTipo()).isEqualTo(InteresProducto.Tipo.FAVORITO);
        assertThat(guardado.getValue().getPeso()).isEqualTo(3);
    }

    @Test
    void perfilUneMarcasYCategoriasQueElPremiumYaDemostroInteres() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(true)));
        InteresProducto nikeCalzado = new InteresProducto(
                usuario(true), producto("Nike", "CALZADO"), InteresProducto.Tipo.VISTA, 5);
        InteresProducto nikeRemera = new InteresProducto(
                usuario(true), producto("nike", "REMERA"), InteresProducto.Tipo.FAVORITO, 3);
        when(interesRepository.findByUsuarioIdAndFechaAfterOrderByFechaDesc(
                eq(7L), any(LocalDateTime.class))).thenReturn(List.of(nikeCalzado, nikeRemera));

        PerfilInteres perfil = service.perfil(7L);

        assertThat(perfil.tieneInteres()).isTrue();
        // Marca saturada (0.15) + categoría REMERA con peso 3/5 (0.06).
        assertThat(perfil.bono(producto(" NIKE ", "remera"))).isEqualTo(0.21);
        assertThat(perfil.bono(producto("Adidas", "VESTIDO"))).isEqualTo(0.0);
    }

    @Test
    void perfilEsVacioParaInvitadoYParaNoPremium() {
        assertThat(service.perfil(null)).isSameAs(PerfilInteres.vacio());
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario(false)));

        assertThat(service.perfil(7L)).isSameAs(PerfilInteres.vacio());
        verifyNoInteractions(interesRepository);
    }
}
