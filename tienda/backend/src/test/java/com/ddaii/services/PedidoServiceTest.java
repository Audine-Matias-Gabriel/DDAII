package com.ddaii.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.Pedido;
import com.ddaii.domain.Producto;
import com.ddaii.repositories.PedidoRepository;
import com.ddaii.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Producto producto(Long id, BigDecimal precio) {
        return new Producto(
                id, "Remera Nike", "descripcion", precio, 10,
                Categoria.REMPERA, 1L, "img.png");
    }

    // --- obtenerPedido ---

    @Test
    void obtenerPedido_idNull_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoService.obtenerPedido(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    void obtenerPedido_idCero_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> pedidoService.obtenerPedido(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPedido_clienteSinPedidos_lanzaNoSuchElementException() {
        when(pedidoRepository.findByClienteId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> pedidoService.obtenerPedido(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("no tiene pedidos");
    }

    @Test
    void obtenerPedido_conPedidos_devuelveLaLista() {
        List<Pedido> pedidos = List.of(new Pedido());
        when(pedidoRepository.findByClienteId(1L)).thenReturn(pedidos);

        assertThat(pedidoService.obtenerPedido(1L)).isEqualTo(pedidos);
    }

    // --- agregarProducto ---

    @Test
    void agregarProducto_creaDetalle_conPrecioDeLaBase_yRecalculaTotal() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        Producto producto = producto(1L, new BigDecimal("10000"));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        pedidoService.agregarProducto(1L, 1L, 2);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());

        Pedido guardado = captor.getValue();
        assertThat(guardado.getDetalles()).hasSize(1);
        assertThat(guardado.getDetalles().get(0).getPrecioUnitario())
                .isEqualByComparingTo("10000");
        assertThat(guardado.getDetalles().get(0).getCantidad()).isEqualTo(2);
        assertThat(guardado.getTotal()).isEqualByComparingTo("20000");
    }

    @Test
    void agregarProducto_pedidoInexistente_lanzaNoSuchElementException() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.agregarProducto(999L, 1L, 1))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void agregarProducto_productoInexistente_lanzaNoSuchElementException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido()));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.agregarProducto(1L, 999L, 1))
                .isInstanceOf(NoSuchElementException.class);
    }
}
