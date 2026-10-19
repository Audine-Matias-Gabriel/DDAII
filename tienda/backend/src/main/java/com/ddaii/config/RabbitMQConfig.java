package com.ddaii.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String COLA_PRUEBA = "prueba.cola";

    @Bean
    public Queue colaPrueba() {
        return new Queue(COLA_PRUEBA, true);
    }

    public static final String COLA_PEDIDO_CREADO =
            "tienda.pedidos.creado";

    @Bean
    public Queue colaPedidoCreado() {
        return new Queue(COLA_PEDIDO_CREADO, true);
    }

}