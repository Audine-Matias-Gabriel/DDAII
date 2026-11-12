package com.ddaii.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Topología de RabbitMQ declarada en {@link AmqpConfig}: un topic exchange,
 * una cola por evento y una DLQ por cola, todas durables.
 */
class AmqpConfigTest {

    private static final String EXCHANGE = "tienda.exchange";

    private AmqpConfig config;

    @BeforeEach
    void setUp() {
        // Los valores por defecto de application.yml (los @Value no se resuelven sin Spring).
        config = new AmqpConfig();
        ReflectionTestUtils.setField(config, "exchange", EXCHANGE);
        ReflectionTestUtils.setField(config, "queueProductoScrapeado", "producto.scrapeado.queue");
        ReflectionTestUtils.setField(config, "queueProductoScrapeadoDlq", "producto.scrapeado.dlq");
        ReflectionTestUtils.setField(config, "queueSuscripcionActivada", "suscripcion.activada.queue");
        ReflectionTestUtils.setField(config, "queueSuscripcionActivadaDlq", "suscripcion.activada.dlq");
        ReflectionTestUtils.setField(config, "queueRecomendacionSolicitada", "recomendacion.solicitada.queue");
        ReflectionTestUtils.setField(config, "queueRecomendacionSolicitadaDlq", "recomendacion.solicitada.dlq");
    }

    @Test
    void declaraElExchangeTopicDurable() {
        TopicExchange exchange = config.tiendaExchange();

        assertThat(exchange.getName()).isEqualTo(EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void declaraSeisColasDurables() {
        List<Queue> colas = List.of(
                config.colaProductoScrapeado(), config.dlqProductoScrapeado(),
                config.colaSuscripcionActivada(), config.dlqSuscripcionActivada(),
                config.colaRecomendacionSolicitada(), config.dlqRecomendacionSolicitada());

        assertThat(colas).extracting(Queue::getName).containsExactlyInAnyOrder(
                "producto.scrapeado.queue", "producto.scrapeado.dlq",
                "suscripcion.activada.queue", "suscripcion.activada.dlq",
                "recomendacion.solicitada.queue", "recomendacion.solicitada.dlq");
        assertThat(colas).allMatch(Queue::isDurable);
    }

    @Test
    void cadaColaMandaSusMensajesMuertosASuDlq() {
        assertDeadLetter(config.colaProductoScrapeado(), "producto.scrapeado.dlq");
        assertDeadLetter(config.colaSuscripcionActivada(), "suscripcion.activada.dlq");
        assertDeadLetter(config.colaRecomendacionSolicitada(), "recomendacion.solicitada.dlq");
    }

    @Test
    void cadaColaEstaEnlazadaAlExchangeConSuRoutingKey() {
        assertBinding(config.bindingProductoScrapeado(), "producto.scrapeado.queue", "producto.scrapeado");
        assertBinding(config.bindingSuscripcionActivada(), "suscripcion.activada.queue", "suscripcion.activada");
        assertBinding(config.bindingRecomendacionSolicitada(), "recomendacion.solicitada.queue", "recomendacion.solicitada");
    }

    @Test
    void cadaDlqEstaEnlazadaAlExchangeConSuNombre() {
        assertBinding(config.bindingDlqProductoScrapeado(), "producto.scrapeado.dlq", "producto.scrapeado.dlq");
        assertBinding(config.bindingDlqSuscripcionActivada(), "suscripcion.activada.dlq", "suscripcion.activada.dlq");
        assertBinding(config.bindingDlqRecomendacionSolicitada(), "recomendacion.solicitada.dlq", "recomendacion.solicitada.dlq");
    }

    private static void assertDeadLetter(Queue cola, String dlqRoutingKey) {
        assertThat(cola.getArguments())
                .containsEntry("x-dead-letter-exchange", EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", dlqRoutingKey);
    }

    private static void assertBinding(Binding binding, String cola, String routingKey) {
        assertThat(binding.getExchange()).isEqualTo(EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(cola);
        assertThat(binding.getRoutingKey()).isEqualTo(routingKey);
    }
}
