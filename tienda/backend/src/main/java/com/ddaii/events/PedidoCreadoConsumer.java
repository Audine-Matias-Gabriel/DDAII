package com.ddaii.events;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCreadoConsumer {

    private final ObjectMapper objectMapper;

    public PedidoCreadoConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "tienda.pedidos.creado")
    public void consumir(String mensaje) {
        try {
            PedidoCreadoEvent evento =
                    objectMapper.readValue(mensaje, PedidoCreadoEvent.class);

            System.out.println("=== PEDIDO RECIBIDO DESDE RABBITMQ ===");
            System.out.println("ID del pedido: " + evento.pedidoId());
            System.out.println("Cantidad de artículos: " + evento.items().size());

            for (PedidoCreadoEvent.ItemPedido item : evento.items()) {
                System.out.println(
                        "Producto: " + item.nombre()
                                + " | ID: " + item.productoId()
                                + " | Género: " + item.genero()
                                + " | Talle: " + item.talle()
                                + " | Cantidad: " + item.cantidad()
                );
            }

        } catch (JacksonException e) {
            throw new AmqpRejectAndDontRequeueException(
                    "Mensaje de pedido con formato JSON inválido", e
            );
        }
    }
}