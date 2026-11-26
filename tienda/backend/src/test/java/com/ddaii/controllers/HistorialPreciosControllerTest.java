package com.ddaii.controllers;

import com.ddaii.dto.PrecioHistorialDTO;
import com.ddaii.services.HistorialPreciosService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorialPreciosControllerTest {

    @Test
    void exponeLaRutaHistorialPrecios() throws NoSuchMethodException {
        RequestMapping clase = HistorialPreciosController.class.getAnnotation(RequestMapping.class);
        GetMapping metodo = HistorialPreciosController.class
                .getMethod("historial", Long.class).getAnnotation(GetMapping.class);

        assertNotNull(clase);
        assertEquals("/api/productos", clase.value()[0]);
        assertNotNull(metodo);
        assertEquals("/{id}/historial-precios", metodo.value()[0]);
    }

    @Test
    void devuelveLaSerieDelServicio() {
        HistorialPreciosService service = mock(HistorialPreciosService.class);
        List<PrecioHistorialDTO> serie = List.of(new PrecioHistorialDTO(LocalDate.of(2026, 11, 25), 1500.0));
        when(service.historial(10L)).thenReturn(serie);

        List<PrecioHistorialDTO> resultado = new HistorialPreciosController(service).historial(10L);

        assertEquals(serie, resultado);
        verify(service).historial(10L);
    }
}
