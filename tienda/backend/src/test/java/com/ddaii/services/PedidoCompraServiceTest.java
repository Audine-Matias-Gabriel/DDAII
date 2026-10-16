package com.ddaii.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.DetallePedido;
import com.ddaii.domain.EstadoPedido;
import com.ddaii.domain.Genero;
import com.ddaii.domain.Pedido;
import com.ddaii.domain.Producto;
import com.ddaii.events.PedidoCreadoEvent;
import com.ddaii.repositories.PedidoRepository;
import com.ddaii.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoCompraServiceTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PedidoCompraService pedidoCompraService;

    private Producto producto(Long id, String nombre, BigDecimal precio) {
        Map<Genero, Map<String, Integer>> mapa = new EnumMap<>(Genero.class);
        mapa.put(Genero.HOMBRE, new HashMap<>(Map.of("M", 10, "L", 10)));

        return new Producto(
                id, nombre, "descripcion", precio, mapa,
                Categoria.REMPERA, 1L, "img.png");
    }

    private Pedido pedidoConDetalle(
            Long productoId,
            Genero genero,
            String talle,
            Integer cantidad) {

        Pedido pedido = new Pedido();
        pedido.setClienteId(1L);

        DetallePedido detalle = new DetallePedido();
        detalle.setProductoId(productoId);
        detalle.setGenero(genero);
        detalle.setTalle(talle);
        detalle.setCantidad(cantidad);
        pedido.agregarDetalle(detalle);

        return pedido;
    }

    private Pedido pedidoConDetalle(Long productoId, Integer cantidad) {
        return pedidoConDetalle(productoId, Genero.HOMBRE, "M", cantidad);
    }

    @Test
    void confirmarCompra_resuelvePrecioDesdeBase_calculaTotal_yPublicaEvento() {
        Producto producto = producto(1L, "Remera Nike", new BigDecimal("10000"));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        Pedido pedido = pedidoConDetalle(1L, 3);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        Pedido guardado = pedidoCompraService.confirmarCompra(pedido);

        DetallePedido detalle = guardado.getDetalles().get(0);
        assertThat(detalle.getPrecioUnitario())
                .isEqualByComparingTo("10000");
        assertThat(detalle.getGenero()).isEqualTo(Genero.HOMBRE);
        assertThat(detalle.getTalle()).isEqualTo("M");
        assertThat(guardado.getTotal()).isEqualByComparingTo("30000");
        assertThat(guardado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(guardado.getTiendaId()).isEqualTo(1L);

        verify(pedidoRepository).save(pedido);
        verify(inventarioService).verificarStock(1L, Genero.HOMBRE, "M", 3);

        ArgumentCaptor<PedidoCreadoEvent> captor =
                ArgumentCaptor.forClass(PedidoCreadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().items()).hasSize(1);
        assertThat(captor.getValue().items().get(0).productoId()).isEqualTo(1L);
        assertThat(captor.getValue().items().get(0).genero()).isEqualTo(Genero.HOMBRE);
        assertThat(captor.getValue().items().get(0).talle()).isEqualTo("M");
        assertThat(captor.getValue().items().get(0).cantidad()).isEqualTo(3);
    }

    @Test
    void confirmarCompra_pedidoSinDetalles_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido();

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(pedido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un detalle");

        verifyNoInteractions(pedidoRepository, eventPublisher);
    }

    @Test
    void confirmarCompra_detalleSinProductoId_lanzaIllegalArgumentException() {
        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido();
        detalle.setCantidad(1);
        pedido.agregarDetalle(detalle);

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(pedido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productoId");
    }

    @Test
    void confirmarCompra_cantidadCero_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    void confirmarCompra_cantidadNull_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confirmarCompra_detalleSinGenero_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, null, "M", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("género");

        verifyNoInteractions(productoRepository, pedidoRepository, eventPublisher);
    }

    @Test
    void confirmarCompra_detalleSinTalle_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, Genero.HOMBRE, null, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("talle");

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, Genero.HOMBRE, "  ", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("talle");
    }

    @Test
    void confirmarCompra_productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(999L, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto inexistente: 999");

        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void confirmarCompra_combinacionInexistente_lanzaIllegalArgumentException() {
        Producto producto = producto(1L, "Remera Nike", new BigDecimal("10000"));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(
                pedidoConDetalle(1L, Genero.HOMBRE, "XL", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Combinación");

        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void confirmarCompra_stockInsuficiente_noGuardaNiPublica() {
        Producto producto = producto(1L, "Remera Nike", new BigDecimal("10000"));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        doThrow(new IllegalStateException("Stock insuficiente"))
                .when(inventarioService).verificarStock(1L, Genero.HOMBRE, "M", 5);

        Pedido pedido = pedidoConDetalle(1L, 5);

        assertThatThrownBy(() -> pedidoCompraService.confirmarCompra(pedido))
                .isInstanceOf(IllegalStateException.class);

        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
