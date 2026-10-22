package com.ddaii.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Cola donde se publica el evento de pedido creado. El exchange vacio
     * enruta directo a esta cola usando su nombre como routing key.
     */
    public static final String COLA_PEDIDO_CREADO =
            "tienda.pedidos.creado";

    /**
     * Cola de mensajes muertos: recibe los mensajes que el consumidor
     * rechaza y no vuelve a encolar.
     */
    public static final String COLA_PEDIDO_CREADO_DLQ =
            "tienda.pedidos.creado.dlq";

    @Bean
    public Queue colaPedidoCreado() {
        return QueueBuilder.durable(COLA_PEDIDO_CREADO)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COLA_PEDIDO_CREADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaPedidoCreadoDlq() {
        return QueueBuilder.durable(COLA_PEDIDO_CREADO_DLQ)
                .build();
    }
}
