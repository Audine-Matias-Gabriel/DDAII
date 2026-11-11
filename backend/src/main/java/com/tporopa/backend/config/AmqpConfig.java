package com.tporopa.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ (P5, Día 2).
 * Un exchange para el sistema, una cola por consumidor y una cola de errores (DLQ) por cola.
 */
@Configuration
public class AmqpConfig {

    @Value("${rabbitmq.exchange:webropa.events}")
    private String exchange;

    @Value("${rabbitmq.queue.producto.scrapeado:cola.producto.scrapeado}")
    private String queueProductoScrapeado;

    @Value("${rabbitmq.queue.suscripcion.activada:cola.suscripcion.activada}")
    private String queueSuscripcionActivada;

    @Value("${rabbitmq.queue.recomendacion.solicitada:cola.recomendacion.solicitada}")
    private String queueRecomendacionSolicitada;

    @Value("${rabbitmq.queue.dlq:cola.errores.dlq}")
    private String queueDlq;

    @Bean
    public TopicExchange webropaExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(queueDlq).build();
    }

    @Bean
    public Queue colaProductoScrapeado() {
        return QueueBuilder.durable(queueProductoScrapeado)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("evento.dlq")
                .build();
    }

    @Bean
    public Queue colaSuscripcionActivada() {
        return QueueBuilder.durable(queueSuscripcionActivada)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("evento.dlq")
                .build();
    }

    @Bean
    public Queue colaRecomendacionSolicitada() {
        return QueueBuilder.durable(queueRecomendacionSolicitada)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("evento.dlq")
                .build();
    }

    @Bean
    public Binding bindingProductoScrapeado() {
        return BindingBuilder.bind(colaProductoScrapeado())
                .to(webropaExchange())
                .with("evento.producto.scrapeado");
    }

    @Bean
    public Binding bindingSuscripcionActivada() {
        return BindingBuilder.bind(colaSuscripcionActivada())
                .to(webropaExchange())
                .with("evento.suscripcion.activada");
    }

    @Bean
    public Binding bindingRecomendacionSolicitada() {
        return BindingBuilder.bind(colaRecomendacionSolicitada())
                .to(webropaExchange())
                .with("evento.recomendacion.solicitada");
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder.bind(dlq()).to(webropaExchange()).with("evento.dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
