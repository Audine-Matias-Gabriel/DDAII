package com.ddaii.events;

import com.ddaii.config.RabbitMQConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PedidoCreadoPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Duration timeoutConfirmacion;

    public PedidoCreadoPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${tienda.rabbitmq.confirm-timeout:5s}")
            Duration timeoutConfirmacion) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.timeoutConfirmacion = timeoutConfirmacion;
    }

    /**
     * Publica el evento del pedido en la cola y espera la confirmación del
     * broker.
     *
     * El exchange vacío es el exchange por defecto de RabbitMQ: enruta el
     * mensaje directamente a la cola cuyo nombre es el routing key.
     *
     * No alcanza con hacer el {@code send()}: sin confirmaciones, un mensaje
     * que el broker rechaza se pierde en silencio y el pedido queda sin
     * notificar. Por eso se espera el ack con un timeout.
     */
    public void publicar(PedidoCreadoEvent evento) {
        Message mensaje = construirMensaje(evento);

        CorrelationData correlationData =
                new CorrelationData("pedido-" + evento.pedidoId());

        rabbitTemplate.send(
                "",
                RabbitMQConfig.COLA_PEDIDO_CREADO,
                mensaje,
                correlationData
        );

        esperarConfirmacion(correlationData, evento.pedidoId());

        log.info(
                "Pedido publicado en RabbitMQ. ID: {}",
                evento.pedidoId()
        );
    }

    private Message construirMensaje(PedidoCreadoEvent evento) {
        String json;
        try {
            json = objectMapper.writeValueAsString(evento);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "No se pudo convertir el evento del pedido a JSON", e
            );
        }

        return MessageBuilder
                .withBody(json.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
    }

    /**
     * Espera el ack del broker. Si el broker rechaza el mensaje (nack) o no
     * responde dentro del timeout, se lanza {@link AmqpException} para que el
     * evento no se dé por publicado.
     */
    private void esperarConfirmacion(
            CorrelationData correlationData,
            Long pedidoId) {

        CompletableFuture<CorrelationData.Confirm> confirmacion =
                correlationData.getFuture();

        CorrelationData.Confirm confirm;
        try {
            confirm = confirmacion.get(
                    timeoutConfirmacion.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        } catch (TimeoutException e) {
            throw new AmqpException(
                    "El broker no confirmó la publicación del pedido "
                            + pedidoId + " en " + timeoutConfirmacion,
                    e
            );
        } catch (ExecutionException e) {
            throw new AmqpException(
                    "Falló la confirmación de la publicación del pedido "
                            + pedidoId,
                    e.getCause()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpException(
                    "Se interrumpió la espera de confirmación del pedido "
                            + pedidoId,
                    e
            );
        }

        if (!confirm.ack()) {
            throw new AmqpException(
                    "El broker rechazó (nack) la publicación del pedido "
                            + pedidoId + ": " + confirm.reason()
            );
        }

        // Un mensaje que no se pudo enrutar vuelve con un ack igual: el
        // broker lo acepta pero lo devuelve. El return siempre llega antes
        // que la confirmacion, asi que se chequea despues de esperarla.
        ReturnedMessage devuelto = correlationData.getReturned();
        if (devuelto != null) {
            throw new AmqpException(
                    "El broker devolvio el mensaje del pedido "
                            + pedidoId + " sin enrutar: "
                            + devuelto.getReplyCode() + " "
                            + devuelto.getReplyText()
            );
        }
    }
}
