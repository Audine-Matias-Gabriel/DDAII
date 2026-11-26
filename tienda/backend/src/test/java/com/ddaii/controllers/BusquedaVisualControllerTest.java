package com.ddaii.controllers;

import com.ddaii.dto.ResultadoVisualDTO;
import com.ddaii.services.BusquedaVisualService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusquedaVisualControllerTest {

    private static final String METODO = "buscarVisual";

    @Test
    void exponeRutaPremiumConMultipartYPrincipal() throws NoSuchMethodException {
        RequestMapping clase = BusquedaVisualController.class.getAnnotation(RequestMapping.class);
        var metodo = BusquedaVisualController.class
                .getMethod(METODO, String.class, MultipartFile.class, int.class);
        PostMapping post = metodo.getAnnotation(PostMapping.class);

        assertNotNull(clase);
        assertEquals("/api/premium", clase.value()[0]);
        assertNotNull(post);
        assertEquals("/busqueda-visual", post.value()[0]);
        assertThat(post.consumes()).containsExactly(MediaType.MULTIPART_FORM_DATA_VALUE);
        assertNotNull(metodo.getParameters()[0].getAnnotation(AuthenticationPrincipal.class));
    }

    @Test
    void pasaPrincipalImagenYLimiteAlServicio() {
        BusquedaVisualService service = mock(BusquedaVisualService.class);
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen", "foto.jpg", "image/jpeg", "bytes".getBytes(StandardCharsets.UTF_8));
        List<ResultadoVisualDTO> esperado = List.of(new ResultadoVisualDTO());
        when(service.buscar(eq("7"), any(MultipartFile.class), eq(3))).thenReturn(esperado);

        List<ResultadoVisualDTO> resultado =
                new BusquedaVisualController(service).buscarVisual("7", imagen, 3);

        assertEquals(esperado, resultado);
        verify(service).buscar("7", imagen, 3);
    }
}
