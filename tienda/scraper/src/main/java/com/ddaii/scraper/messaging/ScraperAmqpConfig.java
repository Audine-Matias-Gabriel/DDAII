package com.ddaii.scraper.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ del scraper (docs/PLAN-INTEGRACION.md 4.1-4.2).
 * Publica ProductoScrapeado en el exchange compartido con el backend.
 * Declara la misma topología (cola + DLQ) para no chocar con el backend.
 */
@Configuration
public class ScraperAmqpConfig {

    @Value("${app.scraper.queue:producto.scrapeado.queue}")
    private String queue;

    @Value("${app.scraper.queue.dlq:producto.scrapeado.dlq}")
    private String queueDlq;

    @Value("${app.scraper.exchange:tienda.exchange}")
    private String exchange;

    @Bean
    public TopicExchange tiendaExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue colaProductoScrapeado() {
        return QueueBuilder.durable(queue)
                .deadLetterExchange(exchange)
                .deadLetterRoutingKey("producto.scrapeado.dlq")
                .build();
    }

    @Bean
    public Queue dlqProductoScrapeado() {
        return QueueBuilder.durable(queueDlq).build();
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
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
