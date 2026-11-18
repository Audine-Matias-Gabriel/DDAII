package com.ddaii.controllers;

import com.ddaii.config.GlobalExceptionHandler;
import com.ddaii.integrations.MercadoPagoFirma;
import com.ddaii.integrations.MercadoPagoGateway;
import com.ddaii.services.SuscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Formatos de notificación de Mercado Pago que acepta POST /api/suscripciones/webhook. */
class SuscripcionWebhookTest {

    private final SuscripcionService service = mock(SuscripcionService.class);
    private MockMvc mvc;

    private MockMvc mvc(String secreto) {
        var controller = new SuscripcionController(service, mock(MercadoPagoGateway.class), new MercadoPagoFirma(secreto));
        return MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @BeforeEach
    void setUp() {
        mvc = mvc("");
        when(service.procesarNotificacionPago(anyLong())).thenReturn(Optional.of("APROBADA"));
    }

    @Test
    void formatoActualPorQuery() throws Exception {
        mvc.perform(post("/api/suscripciones/webhook").param("type", "payment").param("data.id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procesado").value(true))
                .andExpect(jsonPath("$.estadoSuscripcion").value("APROBADA"));
        verify(service).procesarNotificacionPago(123L);
    }

    @Test
    void formatoActualPorCuerpo() throws Exception {
        mvc.perform(post("/api/suscripciones/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"payment\",\"action\":\"payment.updated\",\"data\":{\"id\":\"456\"}}"))
                .andExpect(status().isOk());
        verify(service).procesarNotificacionPago(456L);
    }

    @Test
    void formatoIpnViejo() throws Exception {
        mvc.perform(post("/api/suscripciones/webhook").param("topic", "payment").param("id", "789"))
                .andExpect(status().isOk());
        verify(service).procesarNotificacionPago(789L);
    }

    @Test
    void otrasNotificacionesSeIgnoranCon200() throws Exception {
        mvc.perform(post("/api/suscripciones/webhook").param("topic", "merchant_order").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procesado").value(false));
        verify(service, never()).procesarNotificacionPago(anyLong());
    }

    @Test
    void conClaveConfiguradaUnaFirmaInvalidaEs401() throws Exception {
        mvc("secreto").perform(post("/api/suscripciones/webhook").param("type", "payment").param("data.id", "123")
                        .header("x-signature", "ts=1,v1=abc").header("x-request-id", "r"))
                .andExpect(status().isUnauthorized());
        verify(service, never()).procesarNotificacionPago(anyLong());
    }

    @Test
    void idNoNumericoEs400() throws Exception {
        mvc.perform(post("/api/suscripciones/webhook").param("type", "payment").param("data.id", "abc"))
                .andExpect(status().isBadRequest());
    }
}
