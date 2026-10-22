package com.ddaii.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.List;

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
import tools.jackson.databind.ObjectMapper;

import com.ddaii.config.RabbitMQConfig;
import com.ddaii.domain.Genero;

@ExtendWith(MockitoExtension.class)
class PedidoCreadoPublisherTest {

    private static final Duration TIMEOUT = Duration.ofMillis(100);

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PedidoCreadoPublisher publisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        publisher = new PedidoCreadoPublisher(
                rabbitTemplate,
                objectMapper,
                TIMEOUT
        );
    }

    private PedidoCreadoEvent evento() {
        return new PedidoCreadoEvent(
                7L,
                List.of(new PedidoCreadoEvent.ItemPedido(
                        1L,
                        "Remera Nike",
                        Genero.HOMBRE,
                        "M",
                        2
                ))
        );
    }

    /** Completa la confirmacion que el broker envia de vuelta. */
    private void brokerConfirma(boolean ack, String motivo) {
        doAnswer(invocacion -> {
            CorrelationData datos = invocacion.getArgument(3);
            datos.getFuture().complete(
                    new CorrelationData.Confirm(ack, motivo));
            return null;
        }).when(rabbitTemplate).send(
                anyString(),
                anyString(),
                any(Message.class),
                any(CorrelationData.class)
        );
    }

    @Test
    void publicar_enviaElEventoALaColaYEsperaElAck() {
        PedidoCreadoEvent evento = evento();
        brokerConfirma(true, null);

        publisher.publicar(evento);

        ArgumentCaptor<Message> mensaje =
                ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<CorrelationData> datos =
                ArgumentCaptor.forClass(CorrelationData.class);

        verify(rabbitTemplate).send(
                eq(""),
                eq(RabbitMQConfig.COLA_PEDIDO_CREADO),
                mensaje.capture(),
                datos.capture()
        );

        Message enviado = mensaje.getValue();
        assertThat(enviado.getMessageProperties().getContentType())
                .isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        assertThat(enviado.getMessageProperties().getDeliveryMode())
                .isEqualTo(MessageDeliveryMode.PERSISTENT);

        PedidoCreadoEvent recibido = objectMapper.readValue(
                enviado.getBody(),
                PedidoCreadoEvent.class
        );
        assertThat(recibido.pedidoId()).isEqualTo(7L);
        assertThat(recibido.items()).hasSize(1);
        assertThat(recibido.items().get(0).nombre())
                .isEqualTo("Remera Nike");
        assertThat(recibido.items().get(0).genero())
                .isEqualTo(Genero.HOMBRE);
    }

    @Test
    void publicar_siElBrokerNackea_lanzaAmqpException() {
        PedidoCreadoEvent evento = evento();
        brokerConfirma(false, "no se pudo guardar");

        assertThatThrownBy(() -> publisher.publicar(evento))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("nack");
    }

    @Test
    void publicar_siElBrokerNoConfirma_lanzaAmqpException() {
        PedidoCreadoEvent evento = evento();

        assertThatThrownBy(() -> publisher.publicar(evento))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("no confirmó");
    }

    @Test
    void publicar_siElBrokerDevuelveElMensajeSinEnrutar_lanzaAmqpException() {
        PedidoCreadoEvent evento = evento();

        doAnswer(invocacion -> {
            CorrelationData datos = invocacion.getArgument(3);
            datos.setReturned(new ReturnedMessage(
                    invocacion.getArgument(2),
                    312,
                    "NO_ROUTE",
                    "",
                    RabbitMQConfig.COLA_PEDIDO_CREADO
            ));
            datos.getFuture().complete(
                    new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).send(
                anyString(),
                anyString(),
                any(Message.class),
                any(CorrelationData.class)
        );

        assertThatThrownBy(() -> publisher.publicar(evento))
                .isInstanceOf(AmqpException.class)
                .hasMessageContaining("sin enrutar");
    }
}
