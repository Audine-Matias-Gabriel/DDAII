package com.ddaii.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ddaii.domain.Categoria;
import com.ddaii.domain.Producto;
import com.ddaii.services.ProductoService;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
    }

    private Producto producto(Long id, Integer stock) {
        return new Producto(
                id, "Remera Nike", "descripcion",
                new BigDecimal("10000"), stock,
                Categoria.REMPERA, 1L, "img.png");
    }

    @Test
    void obtenerTodos_devuelve200() throws Exception {
        when(productoService.obtenerTodos())
                .thenReturn(List.of(producto(1L, 5)));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Remera Nike"));
    }

    @Test
    void obtenerPorId_existente_devuelve200() throws Exception {
        when(productoService.obtenerPorId(1L))
                .thenReturn(producto(1L, 5));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(productoService.obtenerPorId(999L))
                .thenThrow(new NoSuchElementException("Producto no encontrado: 999"));

        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Producto no encontrado: 999"));
    }

    @Test
    void buscarPorTienda_devuelve200() throws Exception {
        when(productoService.buscarPorTienda(1L))
                .thenReturn(List.of(producto(1L, 5)));

        mockMvc.perform(get("/api/productos/tienda/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tiendaId").value(1));
    }

    @Test
    void actualizarStock_ok_devuelve200() throws Exception {
        when(productoService.actualizarStock(1L, 3))
                .thenReturn(producto(1L, 7));

        mockMvc.perform(put("/api/productos/1/stock")
                        .param("cantidad", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void actualizarStock_inexistente_devuelve404() throws Exception {
        when(productoService.actualizarStock(999L, 3))
                .thenThrow(new NoSuchElementException("Producto no encontrado: 999"));

        mockMvc.perform(put("/api/productos/999/stock")
                        .param("cantidad", "3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Producto no encontrado: 999"));
    }

    @Test
    void actualizarStock_insuficiente_devuelve409() throws Exception {
        when(productoService.actualizarStock(1L, 50))
                .thenThrow(new IllegalStateException("Stock insuficiente"));

        mockMvc.perform(put("/api/productos/1/stock")
                        .param("cantidad", "50"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Stock insuficiente"));
    }

    @Test
    void actualizarStock_cantidadInvalida_devuelve400() throws Exception {
        when(productoService.actualizarStock(1L, 0))
                .thenThrow(new IllegalArgumentException("La cantidad debe ser mayor a 0"));

        mockMvc.perform(put("/api/productos/1/stock")
                        .param("cantidad", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La cantidad debe ser mayor a 0"));
    }

    @Test
    void actualizarStock_delegaEnElServicio() throws Exception {
        when(productoService.actualizarStock(any(), any()))
                .thenReturn(producto(1L, 7));

        mockMvc.perform(put("/api/productos/1/stock")
                        .param("cantidad", "3"))
                .andExpect(status().isOk());

        verify(productoService).actualizarStock(1L, 3);
    }
}
