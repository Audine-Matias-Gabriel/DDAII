package com.ddaii.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.ddaii.config.RabbitMQConfig;

@ExtendWith(MockitoExtension.class)
class EventoPublisherTest {

    private static final Duration TIMEOUT = Duration.ofMillis(100);
    private static final String ROUTING_KEY = RabbitMQConfig.RK_SUSCRIPCION_ACTIVADA;
    private static final String ID = "suscripcion-1";

    /** Evento de prueba con la forma de SuscripcionActivada (docs/eventos.md). */
    record EventoDePrueba(Long suscripcionId, Long usuarioId, String pagoId, LocalDateTime fecha) {}

    @Mock
    private RabbitTemplate rabbitTemplate;

    private EventoPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        publisher = new EventoPublisher(rabbitTemplate, objectMapper, TIMEOUT);
    }

    private EventoDePrueba evento() {
        return new EventoDePrueba(1L, 7L, "123456", LocalDateTime.of(2026, 11, 12, 10, 0));
    }

    /** Completa la confirmacion que el broker envia de vuelta. */
    private void brokerConfirma(boolean ack, String motivo) {
        doAnswer(invocacion -> {
            CorrelationData datos = invocacion.getArgument(3);
            datos.getFuture().complete(new CorrelationData.Confirm(ack, motivo));
            return null;
        }).when(rabbitTemplate).send(
                anyString(),
                anyString(),
                any(Message.class),
                any(CorrelationData.class)
        );
    }

    @Test
    void publicar_enviaElEventoAlExchangeConSuRoutingKeyYEsperaElAck() {
        brokerConfirma(true, null);

        publisher.publicar(ROUTING_KEY, evento(), ID);

        ArgumentCaptor<Message> mensaje = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<CorrelationData> datos = ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).send(
                eq(RabbitMQConfig.EXCHANGE),
                eq(ROUTING_KEY),
                mensaje.capture(),
                datos.capture()
        );

        assertThat(datos.getValue().getId()).isEqualTo(ID);

        MessageProperties propiedades = mensaje.getValue().getMessageProperties();
        assertThat(propiedades.getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        assertThat(propiedades.getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(propiedades.getMessageId()).isEqualTo(ID);

        JsonNode json = objectMapper.readTree(mensaje.getValue().getBody());
        assertThat(json.get("suscripcionId").asLong()).isEqualTo(1L);
        assertThat(json.get("usuarioId").asLong()).isEqualTo(7L);
        assertThat(json.get("pagoId").asString()).isEqualTo("123456");
        assertThat(json.get("fecha").asString()).isEqualTo("2026-11-12T10:00:00");
    }

    @Test
    void publicar_siElBrokerNackea_lanzaAmqpException() {
        brokerConfirma(false, "no se pudo guardar");

        assertThatThrownBy(() -> publisher.publicar(ROUTING_KEY, evento(), ID))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("nack")
                .hasMessageContaining(ID);
    }

    @Test
    void publicar_siElBrokerNoConfirma_lanzaAmqpException() {
        assertThatThrownBy(() -> publisher.publicar(ROUTING_KEY, evento(), ID))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("no confirmó");
    }

    @Test
    void publicar_siElBrokerDevuelveElMensajeSinEnrutar_lanzaAmqpException() {
        doAnswer(invocacion -> {
            CorrelationData datos = invocacion.getArgument(3);
            datos.setReturned(new ReturnedMessage(
                    invocacion.getArgument(2),
                    312,
                    "NO_ROUTE",
                    RabbitMQConfig.EXCHANGE,
                    ROUTING_KEY
            ));
            datos.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(
                anyString(),
                anyString(),
                any(Message.class),
                any(CorrelationData.class)
        );

        assertThatThrownBy(() -> publisher.publicar(ROUTING_KEY, evento(), ID))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("sin enrutar");
    }
}
