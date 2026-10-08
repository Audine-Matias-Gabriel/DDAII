package com.ddaii.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.Producto;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.events.PedidoCreadoEvent.ItemPedido;
import com.ddaii.events.StockActualizadoEvent;
import com.ddaii.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InventarioService inventarioService;

    private Producto productoConStock(Long id, Integer stock) {
        return new Producto(
                id,
                "Remera Nike",
                "Remera deportiva",
                new BigDecimal("10000"),
                stock,
                Categoria.REMPERA,
                1L,
                "remera.png");
    }

    // --- Los 3 casos obligatorios del enunciado ---

    @Test
    void compraValida_descuentaStock_yPublicaEvento() {
        Producto producto = productoConStock(1L, 10);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        inventarioService.actualizarStock(50L, 1L, 3);

        assertThat(producto.getStock()).isEqualTo(7);
        verify(productoRepository).save(producto);

        ArgumentCaptor<StockActualizadoEvent> captor =
                ArgumentCaptor.forClass(StockActualizadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        StockActualizadoEvent evento = captor.getValue();
        assertThat(evento.pedidoId()).isEqualTo(50L);
        assertThat(evento.productoId()).isEqualTo(1L);
        assertThat(evento.cantidadVendida()).isEqualTo(3);
        assertThat(evento.stockAnterior()).isEqualTo(10);
        assertThat(evento.stockActual()).isEqualTo(7);
    }

    @Test
    void stockInsuficiente_lanzaIllegalStateException_yNoModificaStock() {
        Producto producto = productoConStock(1L, 2);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> inventarioService.actualizarStock(50L, 1L, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(producto.getStock()).isEqualTo(2);
        verify(productoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.actualizarStock(50L, 999L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto inexistente: 999");
    }

    // --- verificarStock ---

    @Test
    void verificarStock_cantidadCero_lanzaIllegalArgumentException_sinConsultarRepo() {
        assertThatThrownBy(() -> inventarioService.verificarStock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");

        verifyNoInteractions(productoRepository);
    }

    @Test
    void verificarStock_cantidadNegativa_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> inventarioService.verificarStock(1L, -3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verificarStock_suficiente_noLanzaNada() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(productoConStock(1L, 10)));

        inventarioService.verificarStock(1L, 10);
    }

    // --- alCrearPedido (Observer) ---

    @Test
    void alCrearPedido_agrupaItemsRepetidos_delMismoProducto() {
        Producto producto = productoConStock(1L, 10);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        inventarioService.alCrearPedido(new PedidoCreadoEvent(50L, List.of(
                new ItemPedido(1L, "Remera Nike", 2),
                new ItemPedido(1L, "Remera Nike", 3))));

        assertThat(producto.getStock()).isEqualTo(5);
    }

    @Test
    void alCrearPedido_validaTodoElStock_antesDeDescontar() {
        Producto conStock = productoConStock(1L, 10);
        Producto sinStock = productoConStock(2L, 1);
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(conStock));
        when(productoRepository.findById(2L))
                .thenReturn(Optional.of(sinStock));

        assertThatThrownBy(() -> inventarioService.alCrearPedido(
                new PedidoCreadoEvent(50L, List.of(
                        new ItemPedido(1L, "Remera Nike", 2),
                        new ItemPedido(2L, "Pantalón", 5)))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(conStock.getStock()).isEqualTo(10);
        assertThat(sinStock.getStock()).isEqualTo(1);
        verifyNoInteractions(eventPublisher);
    }
}
