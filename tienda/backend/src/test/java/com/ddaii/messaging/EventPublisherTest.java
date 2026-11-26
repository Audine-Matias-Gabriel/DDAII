package com.ddaii.messaging;

import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.events.SuscripcionActivada;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Cada evento sale por tienda.exchange con la routing key de su cola (AmqpConfig). */
class EventPublisherTest {

    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final EventPublisher publisher = new EventPublisher(rabbit);

    {
        ReflectionTestUtils.setField(publisher, "exchange", "tienda.exchange");
    }

    @Test
    void publicaSuscripcionActivadaConSusDatos() {
        publisher.publicarSuscripcionActivada(4L, 5L, "190338481640");

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(eq("tienda.exchange"), eq("suscripcion.activada"), evento.capture());
        SuscripcionActivada e = (SuscripcionActivada) evento.getValue();
        assertThat(e.getUsuarioId()).isEqualTo(4L);
        assertThat(e.getSuscripcionId()).isEqualTo(5L);
        assertThat(e.getIdPago()).isEqualTo("190338481640");
    }

    @Test
    void publicaRecomendacionSolicitada() {
        publisher.publicarRecomendacionSolicitada("sol-1", 36L, 3);

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(eq("tienda.exchange"), eq("recomendacion.solicitada"), evento.capture());
        RecomendacionSolicitada e = (RecomendacionSolicitada) evento.getValue();
        assertThat(e.getSolicitudId()).isEqualTo("sol-1");
        assertThat(e.getProductoId()).isEqualTo(36L);
        assertThat(e.getLimite()).isEqualTo(3);
        assertThat(e.getUsuarioId()).isNull();
    }

    @Test
    void laSolicitudPorColaLlevaElUsuarioPremium() {
        publisher.publicarRecomendacionSolicitada("sol-2", 36L, 3, 7L);

        ArgumentCaptor<Object> evento = ArgumentCaptor.forClass(Object.class);
        verify(rabbit).convertAndSend(eq("tienda.exchange"), eq("recomendacion.solicitada"), evento.capture());
        RecomendacionSolicitada e = (RecomendacionSolicitada) evento.getValue();
        assertThat(e.getUsuarioId()).isEqualTo(7L);
    }
}
