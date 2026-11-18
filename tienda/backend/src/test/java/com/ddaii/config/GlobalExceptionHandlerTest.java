package com.ddaii.config;

import com.ddaii.exceptions.RecursoNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Los errores del cliente son 4xx con un mensaje claro; los 5xx no muestran detalles internos. */
class GlobalExceptionHandlerTest {

    @RestController
    static class ControladorDePrueba {
        @GetMapping("/p/{id}")
        String porId(@PathVariable Long id) {
            if (id == 9) throw new RecursoNoEncontradoException("Producto no encontrado: 9");
            if (id == 5) throw new IllegalStateException("detalle interno con datos sensibles");
            return "ok";
        }

        @GetMapping("/p")
        String conParametro(@RequestParam int tamano) {
            return "ok";
        }

        @PostMapping("/p")
        String conCuerpo(@RequestBody Map<String, Object> body) {
            throw new IllegalArgumentException("Ya sos premium: no hace falta volver a pagar.");
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void noEncontradoEs404ConMensaje() throws Exception {
        mvc.perform(get("/p/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado: 9"))
                .andExpect(jsonPath("$.ruta").value("/p/9"));
    }

    @Test
    void idQueNoEsNumeroEs400() throws Exception {
        mvc.perform(get("/p/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'id' tiene un valor inválido: 'abc' (se esperaba Long)"));
    }

    @Test
    void parametroFaltanteOInvalidoEs400() throws Exception {
        mvc.perform(get("/p")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro 'tamano'"));
        mvc.perform(get("/p").param("tamano", "xx")).andExpect(status().isBadRequest());
    }

    @Test
    void jsonMalFormadoEs400() throws Exception {
        mvc.perform(post("/p").contentType(MediaType.APPLICATION_JSON).content("{malformado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo no es un JSON válido"));
    }

    @Test
    void errorDeNegocioEs400ConElMensajeParaElUsuario() throws Exception {
        mvc.perform(post("/p").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya sos premium: no hace falta volver a pagar."));
    }

    @Test
    void errorInesperadoEs500SinDetallesInternos() throws Exception {
        mvc.perform(get("/p/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error del servidor"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado. Probá de nuevo más tarde."));
    }
}
