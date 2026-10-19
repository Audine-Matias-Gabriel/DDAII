package com.ddaii.events;

import com.ddaii.config.RabbitMQConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoCreadoPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PedidoCreadoPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicar(PedidoCreadoEvent evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);

            Message mensaje = MessageBuilder
                    .withBody(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setContentEncoding("UTF-8")
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();

            rabbitTemplate.send(
                    "",
                    RabbitMQConfig.COLA_PEDIDO_CREADO,
                    mensaje
            );

            System.out.println(
                    "Pedido publicado en RabbitMQ. ID: " + evento.pedidoId()
            );

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "No se pudo convertir el evento del pedido a JSON", e
            );
        }
    }
}