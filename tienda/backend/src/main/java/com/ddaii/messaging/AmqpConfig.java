package com.ddaii.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ (docs/PLAN-INTEGRACION.md 4.1).
 * Un topic exchange, una cola por consumidor y una DLQ por cola.
 */
@Configuration
public class AmqpConfig {

    @Value("${spring.rabbitmq.exchange:tienda.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.queue.producto.scrapeado:producto.scrapeado.queue}")
    private String queueProductoScrapeado;

    @Value("${spring.rabbitmq.queue.producto.scrapeado.dlq:producto.scrapeado.dlq}")
    private String queueProductoScrapeadoDlq;

    @Value("${spring.rabbitmq.queue.suscripcion.activada:suscripcion.activada.queue}")
    private String queueSuscripcionActivada;

    @Value("${spring.rabbitmq.queue.suscripcion.activada.dlq:suscripcion.activada.dlq}")
    private String queueSuscripcionActivadaDlq;

    @Value("${spring.rabbitmq.queue.recomendacion.solicitada:recomendacion.solicitada.queue}")
    private String queueRecomendacionSolicitada;

    @Value("${spring.rabbitmq.queue.recomendacion.solicitada.dlq:recomendacion.solicitada.dlq}")
    private String queueRecomendacionSolicitadaDlq;

    @Bean
    public TopicExchange tiendaExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue colaProductoScrapeado() {
        return QueueBuilder.durable(queueProductoScrapeado)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("producto.scrapeado.dlq")
                .build();
    }

    @Bean
    public Queue dlqProductoScrapeado() {
        return QueueBuilder.durable(queueProductoScrapeadoDlq).build();
    }

    @Bean
    public Queue colaSuscripcionActivada() {
        return QueueBuilder.durable(queueSuscripcionActivada)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("suscripcion.activada.dlq")
                .build();
    }

    @Bean
    public Queue dlqSuscripcionActivada() {
        return QueueBuilder.durable(queueSuscripcionActivadaDlq).build();
    }

    @Bean
    public Queue colaRecomendacionSolicitada() {
        return QueueBuilder.durable(queueRecomendacionSolicitada)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("recomendacion.solicitada.dlq")
                .build();
    }

    @Bean
    public Queue dlqRecomendacionSolicitada() {
        return QueueBuilder.durable(queueRecomendacionSolicitadaDlq).build();
    }

    @Bean
    public Binding bindingProductoScrapeado() {
        return BindingBuilder.bind(colaProductoScrapeado())
                .to(tiendaExchange())
                .with("producto.scrapeado");
    }

    @Bean
    public Binding bindingDlqProductoScrapeado() {
        return BindingBuilder.bind(dlqProductoScrapeado())
                .to(tiendaExchange())
                .with("producto.scrapeado.dlq");
    }

    @Bean
    public Binding bindingSuscripcionActivada() {
        return BindingBuilder.bind(colaSuscripcionActivada())
                .to(tiendaExchange())
                .with("suscripcion.activada");
    }

    @Bean
    public Binding bindingDlqSuscripcionActivada() {
        return BindingBuilder.bind(dlqSuscripcionActivada())
                .to(tiendaExchange())
                .with("suscripcion.activada.dlq");
    }

    @Bean
    public Binding bindingRecomendacionSolicitada() {
        return BindingBuilder.bind(colaRecomendacionSolicitada())
                .to(tiendaExchange())
                .with("recomendacion.solicitada");
    }

    @Bean
    public Binding bindingDlqRecomendacionSolicitada() {
        return BindingBuilder.bind(dlqRecomendacionSolicitada())
                .to(tiendaExchange())
                .with("recomendacion.solicitada.dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        template.setMandatory(true);
        return template;
    }
}
