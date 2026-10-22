package com.ddaii.events;

import com.ddaii.config.RabbitMQConfig;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCreadoConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoCreadoConsumer.class);

    private final ObjectMapper objectMapper;

    public PedidoCreadoConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.COLA_PEDIDO_CREADO)
    public void consumir(String mensaje) {
        PedidoCreadoEvent evento;
        try {
            evento = objectMapper.readValue(
                    mensaje,
                    PedidoCreadoEvent.class
            );
        } catch (JacksonException e) {
            // No se re-encola: el mensaje va a la cola de mensajes muertos.
            throw new AmqpRejectAndDontRequeueException(
                    "Mensaje de pedido con formato JSON inválido", e
            );
        }

        log.info("=== PEDIDO RECIBIDO DESDE RABBITMQ ===");
        log.info("ID del pedido: {}", evento.pedidoId());
        log.info(
                "Cantidad de artículos: {}",
                evento.items().size()
        );

        for (PedidoCreadoEvent.ItemPedido item : evento.items()) {
            log.info(
                    "Producto: {} | ID: {} | Género: {} | Talle: {} | Cantidad: {}",
                    item.nombre(),
                    item.productoId(),
                    item.genero(),
                    item.talle(),
                    item.cantidad()
            );
        }
    }
}
