package com.ddaii.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ddaii.domain.EstadoPedido;
import com.ddaii.domain.Pedido;
import com.ddaii.services.PedidoCompraService;
import com.ddaii.services.PedidoService;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private PedidoCompraService pedidoCompraService;

    @InjectMocks
    private PedidoController pedidoController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(pedidoController).build();
    }

    // --- POST /api/pedidos ---

    @Test
    void confirmarCompra_ok_devuelve201() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        when(pedidoCompraService.confirmarCompra(any(Pedido.class)))
                .thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":1,"cantidad":2,"talle":"M","genero":"HOMBRE"}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void confirmarCompra_stockInsuficiente_devuelve409() throws Exception {
        when(pedidoCompraService.confirmarCompra(any(Pedido.class)))
                .thenThrow(new IllegalStateException("Stock insuficiente para: Remera Nike. Disponible: 2"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":1,"cantidad":5,"talle":"M","genero":"HOMBRE"}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(
                        "Stock insuficiente para: Remera Nike. Disponible: 2"));
    }

    @Test
    void confirmarCompra_validacion_devuelve400() throws Exception {
        when(pedidoCompraService.confirmarCompra(any(Pedido.class)))
                .thenThrow(new IllegalArgumentException("Producto inexistente: 999"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":999,"cantidad":1,"talle":"M","genero":"HOMBRE"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Producto inexistente: 999"));
    }

    @Test
    void confirmarCompra_sinVariante_devuelve400() throws Exception {
        when(pedidoCompraService.confirmarCompra(any(Pedido.class)))
                .thenThrow(new IllegalArgumentException(
                        "Cada detalle debe indicar el talle del producto 1"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"detalles":[{"productoId":1,"cantidad":1,"genero":"HOMBRE"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Cada detalle debe indicar el talle del producto 1"));
    }

    // --- GET /api/pedidos/{clienteId}/pedidos ---

    @Test
    void obtenerPedidos_ok_devuelve200() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        when(pedidoService.obtenerPedido(1L)).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/pedidos/1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPedidos_idInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/pedidos/0/pedidos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPedidos_sinPedidos_devuelve404() throws Exception {
        when(pedidoService.obtenerPedido(1L))
                .thenThrow(new NoSuchElementException("El cliente no tiene pedidos"));

        mockMvc.perform(get("/api/pedidos/1/pedidos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El cliente no tiene pedidos"));
    }

    @Test
    void obtenerPedidos_delegaEnElServicio() throws Exception {
        when(pedidoService.obtenerPedido(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos/1/pedidos"))
                .andExpect(status().isOk());

        verify(pedidoService).obtenerPedido(1L);
    }
}
