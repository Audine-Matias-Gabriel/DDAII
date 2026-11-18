package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Tienda;
import com.ddaii.dto.ProductoDTO;
import com.ddaii.events.ProductoScrapeado;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.TiendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de ProductoService (docs/PLAN-INTEGRACION.md 2.1 y 4.2).
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private TiendaRepository tiendaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Tienda tienda() {
        Tienda t = new Tienda("tienda1", "http://tienda1.com", true);
        t.setId(1L);
        return t;
    }

    @Test
    void upsertDesdeScrapeado_creaProductoCuandoNoExiste() {
        Tienda t = tienda();
        ProductoScrapeado evento = new ProductoScrapeado(
                1L, "tienda1", "EXT-001", "Remera test", "desc", 1000.0,
                "REMERA", "Mujer", "MarcaX", "http://tienda1.com/p/1",
                "http://tienda1.com/img/1.jpg", true, List.of("S", "M"));

        when(tiendaRepository.findByNombre("tienda1")).thenReturn(Optional.of(t));
        when(productoRepository.findByTienda_IdAndIdExterno(1L, "EXT-001")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.upsertDesdeScrapeado(evento);

        assertNotNull(resultado);
        assertEquals("tienda1", resultado.getTienda().getNombre());
        assertEquals("EXT-001", resultado.getIdExterno());
        assertEquals("Remera test", resultado.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void upsertDesdeScrapeado_actualizaProductoCuandoYaExiste() {
        Tienda t = tienda();
        Producto existente = new Producto();
        existente.setTienda(t);
        existente.setIdExterno("EXT-001");
        existente.setNombre("Nombre viejo");

        ProductoScrapeado evento = new ProductoScrapeado(
                1L, "tienda1", "EXT-001", "Remera nueva", "desc", 2000.0,
                "REMERA", "Mujer", "MarcaY", "http://tienda1.com/p/2",
                "http://tienda1.com/img/2.jpg", false, List.of("L"));

        when(tiendaRepository.findByNombre("tienda1")).thenReturn(Optional.of(t));
        when(productoRepository.findByTienda_IdAndIdExterno(1L, "EXT-001")).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.upsertDesdeScrapeado(evento);

        assertEquals("Remera nueva", resultado.getNombre());
        assertEquals(2000.0, resultado.getPrecio());
        assertEquals(Boolean.FALSE, resultado.getDisponible());
        assertEquals(List.of("L"), resultado.getTallesDisponibles());
    }

    @Test
    void upsertDesdeScrapeado_creaLaTiendaConElDominioDeLaUrlOriginal() {
        ProductoScrapeado evento = new ProductoScrapeado(
                2L, "Grid", "52974", "Remera", "desc", 1000.0, "REMERA", "Hombre", "NBA",
                "https://www.grid.com.ar/remera-nba/p", "https://img/1.jpg", true, List.of("M"));
        when(tiendaRepository.findByNombre("Grid")).thenReturn(Optional.empty());
        when(tiendaRepository.save(any(Tienda.class))).thenAnswer(inv -> {
            Tienda nueva = inv.getArgument(0);
            nueva.setId(7L);
            return nueva;
        });
        when(productoRepository.findByTienda_IdAndIdExterno(7L, "52974")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.upsertDesdeScrapeado(evento);

        assertEquals("https://www.grid.com.ar", resultado.getTienda().getUrlBase());
        // Con nombre, el tiendaId del evento no se usa (podría ser el id de otra tienda).
        verify(tiendaRepository, never()).findById(any());
    }

    @Test
    void upsertDesdeScrapeado_corrigeLaUrlInventadaDeUnaTiendaExistente() {
        Tienda vieja = new Tienda("Grid", "https://Grid.com.ar", true);
        vieja.setId(2L);
        ProductoScrapeado evento = new ProductoScrapeado(
                2L, "Grid", "1", "Remera", "d", 1.0, "REMERA", "Hombre", "M",
                "https://www.grid.com.ar/x/p", "https://img/x.jpg", true, List.of());
        when(tiendaRepository.findByNombre("Grid")).thenReturn(Optional.of(vieja));
        when(productoRepository.findByTienda_IdAndIdExterno(2L, "1")).thenReturn(Optional.empty());
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.upsertDesdeScrapeado(evento);

        assertEquals("https://www.grid.com.ar", vieja.getUrlBase());
        verify(tiendaRepository).save(vieja);
    }

    @Test
    void urlBaseDeSoloAceptaUrlsHttp() {
        assertEquals("https://www.sportline.com.ar", ProductoService.urlBaseDe("https://www.sportline.com.ar/boris/p?x=1"));
        assertEquals("http://localhost:8080", ProductoService.urlBaseDe("http://localhost:8080/a"));
        assertNull(ProductoService.urlBaseDe("ftp://algo/x"));
        assertNull(ProductoService.urlBaseDe("no es una url"));
        assertNull(ProductoService.urlBaseDe(null));
    }

    @Test
    void filtrarDevuelvePaginaDeProductos() {
        Tienda t = tienda();
        Producto p = new Producto("EXT-1", t, "Remera", "desc", 1000.0,
                "REMERA", "Mujer", "M", "url", "img", true);
        p.setId(1L);

        when(productoRepository.filtrar(null, null, null, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1));

        Page<ProductoDTO> pagina = productoService.filtrar(
                null, null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Remera", pagina.getContent().get(0).getNombre());
        assertTrue(pagina.getContent().get(0).getDisponible());
        assertEquals("tienda1", pagina.getContent().get(0).getTienda().getNombre());
    }
}
