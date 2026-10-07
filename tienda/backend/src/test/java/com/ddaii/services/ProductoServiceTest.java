package com.ddaii.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.Producto;
import com.ddaii.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto(Long id, Integer stock) {
        return new Producto(
                id, "Remera Nike", "descripcion",
                new BigDecimal("10000"), stock,
                Categoria.REMPERA, 1L, "img.png");
    }

    @Test
    void obtenerTodos_devuelveLaListaDelRepositorio() {
        List<Producto> productos = List.of(producto(1L, 5));
        when(productoRepository.findAll()).thenReturn(productos);

        assertThat(productoService.obtenerTodos()).isEqualTo(productos);
    }

    @Test
    void obtenerPorId_existente_devuelveElProducto() {
        Producto producto = producto(1L, 5);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        assertThat(productoService.obtenerPorId(1L)).isEqualTo(producto);
    }

    @Test
    void obtenerPorId_inexistente_lanzaNoSuchElementException() {
        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Producto no encontrado: 999");
    }

    @Test
    void buscarPorTienda_delegaEnElRepositorio() {
        List<Producto> productos = List.of(producto(1L, 5));
        when(productoRepository.findByTiendaId(1L)).thenReturn(productos);

        assertThat(productoService.buscarPorTienda(1L)).isEqualTo(productos);
    }

    @Test
    void actualizarStock_descuenta_yGuarda() {
        Producto producto = producto(1L, 10);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.actualizarStock(1L, 3);

        assertThat(resultado.getStock()).isEqualTo(7);
        verify(productoRepository).save(producto);
    }

    @Test
    void actualizarStock_inexistente_lanzaNoSuchElementException() {
        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizarStock(999L, 1))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void actualizarStock_cantidadInvalida_lanzaIllegalArgumentException() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto(1L, 10)));

        assertThatThrownBy(() -> productoService.actualizarStock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizarStock_insuficiente_lanzaIllegalStateException() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto(1L, 2)));

        assertThatThrownBy(() -> productoService.actualizarStock(1L, 5))
                .isInstanceOf(IllegalStateException.class);
    }
}
