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

/**
 * Publica cualquier evento del sistema en el exchange
 * {@link RabbitMQConfig#EXCHANGE}. La routing key decide a qué cola llega
 * (ver docs/eventos.md).
 */
@Component
public class EventoPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(EventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Duration timeoutConfirmacion;

    public EventoPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${tienda.rabbitmq.confirm-timeout:5s}")
            Duration timeoutConfirmacion) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.timeoutConfirmacion = timeoutConfirmacion;
    }

    /**
     * Publica el evento como JSON y espera la confirmación del broker.
     *
     * No alcanza con hacer el {@code send()}: sin confirmaciones, un mensaje
     * que el broker rechaza se pierde en silencio. Por eso se espera el ack
     * con un timeout.
     *
     * @param routingKey    routing key del evento, por ejemplo
     *                      {@link RabbitMQConfig#RK_SUSCRIPCION_ACTIVADA}
     * @param evento        objeto que se serializa a JSON
     * @param idCorrelacion identificador único del evento; se usa como
     *                      {@code message_id} y en los logs
     */
    public void publicar(String routingKey, Object evento, String idCorrelacion) {
        Message mensaje = construirMensaje(evento, idCorrelacion);

        CorrelationData correlationData = new CorrelationData(idCorrelacion);

        rabbitTemplate.send(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                mensaje,
                correlationData
        );

        esperarConfirmacion(correlationData, routingKey, idCorrelacion);

        log.info(
                "Evento publicado en RabbitMQ. routingKey={} id={}",
                routingKey,
                idCorrelacion
        );
    }

    private Message construirMensaje(Object evento, String idCorrelacion) {
        String json;
        try {
            json = objectMapper.writeValueAsString(evento);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "No se pudo convertir el evento " + idCorrelacion + " a JSON", e
            );
        }

        return MessageBuilder
                .withBody(json.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(idCorrelacion)
                .build();
    }

    /**
     * Espera el ack del broker. Si el broker rechaza el mensaje (nack) o no
     * responde dentro del timeout, se lanza {@link AmqpException} para que el
     * evento no se dé por publicado.
     */
    private void esperarConfirmacion(
            CorrelationData correlationData,
            String routingKey,
            String idCorrelacion) {

        String evento = idCorrelacion + " (" + routingKey + ")";

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
                    "El broker no confirmó la publicación del evento "
                            + evento + " en " + timeoutConfirmacion,
                    e
            );
        } catch (ExecutionException e) {
            throw new AmqpException(
                    "Falló la confirmación de la publicación del evento "
                            + evento,
                    e.getCause()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpException(
                    "Se interrumpió la espera de confirmación del evento "
                            + evento,
                    e
            );
        }

        if (!confirm.ack()) {
            throw new AmqpException(
                    "El broker rechazó (nack) la publicación del evento "
                            + evento + ": " + confirm.reason()
            );
        }

        // Un mensaje que no se pudo enrutar vuelve con un ack igual: el
        // broker lo acepta pero lo devuelve. El return siempre llega antes
        // que la confirmacion, asi que se chequea despues de esperarla.
        ReturnedMessage devuelto = correlationData.getReturned();
        if (devuelto != null) {
            throw new AmqpException(
                    "El broker devolvio el evento "
                            + evento + " sin enrutar: "
                            + devuelto.getReplyCode() + " "
                            + devuelto.getReplyText()
            );
        }
    }
}
