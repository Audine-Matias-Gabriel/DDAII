package com.ddaii.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final Declarables topologia = new RabbitMQConfig().topologiaEventos();

    private static final Map<String, String> COLA_POR_ROUTING_KEY = Map.of(
            RabbitMQConfig.RK_PRODUCTO_SCRAPEADO, RabbitMQConfig.COLA_PRODUCTO_SCRAPEADO,
            RabbitMQConfig.RK_SUSCRIPCION_ACTIVADA, RabbitMQConfig.COLA_SUSCRIPCION_ACTIVADA,
            RabbitMQConfig.RK_RECOMENDACION_SOLICITADA, RabbitMQConfig.COLA_RECOMENDACION_SOLICITADA
    );

    @Test
    void declaraElExchangeTopicYElDlxDurables() {
        List<Exchange> exchanges = topologia.getDeclarablesByType(Exchange.class);

        assertThat(exchanges).hasSize(2);
        assertThat(exchanges).anySatisfy(e -> {
            assertThat(e).isInstanceOf(TopicExchange.class);
            assertThat(e.getName()).isEqualTo(RabbitMQConfig.EXCHANGE);
            assertThat(e.isDurable()).isTrue();
        });
        assertThat(exchanges).anySatisfy(e -> {
            assertThat(e).isInstanceOf(DirectExchange.class);
            assertThat(e.getName()).isEqualTo(RabbitMQConfig.DLX);
            assertThat(e.isDurable()).isTrue();
        });
    }

    @Test
    void declaraSeisColasDurables() {
        List<Queue> colas = topologia.getDeclarablesByType(Queue.class);

        assertThat(colas).extracting(Queue::getName).containsExactlyInAnyOrder(
                "backend.producto-scrapeado",
                "backend.producto-scrapeado.dlq",
                "backend.suscripcion-activada",
                "backend.suscripcion-activada.dlq",
                "backend.recomendacion-solicitada",
                "backend.recomendacion-solicitada.dlq"
        );
        assertThat(colas).allMatch(Queue::isDurable);
    }

    @Test
    void cadaColaMandaSusMensajesMuertosASuDlq() {
        for (String nombre : COLA_POR_ROUTING_KEY.values()) {
            Queue cola = cola(nombre);

            assertThat(cola.getArguments())
                    .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DLX)
                    .containsEntry("x-dead-letter-routing-key", nombre + ".dlq");
        }
    }

    @Test
    void cadaColaEstaEnlazadaAlExchangeConSuRoutingKey() {
        Map<String, String> colaPorRoutingKey = bindingsDe(RabbitMQConfig.EXCHANGE);

        assertThat(colaPorRoutingKey).isEqualTo(COLA_POR_ROUTING_KEY);
    }

    @Test
    void cadaDlqEstaEnlazadaAlDlxConSuNombre() {
        Map<String, String> dlqPorRoutingKey = bindingsDe(RabbitMQConfig.DLX);

        assertThat(dlqPorRoutingKey).hasSize(3);
        dlqPorRoutingKey.forEach((routingKey, dlq) -> {
            assertThat(dlq).endsWith(".dlq");
            assertThat(routingKey).isEqualTo(dlq);
        });
    }

    private Queue cola(String nombre) {
        return topologia.getDeclarablesByType(Queue.class).stream()
                .filter(q -> q.getName().equals(nombre))
                .findFirst()
                .orElseThrow();
    }

    private Map<String, String> bindingsDe(String exchange) {
        return topologia.getDeclarablesByType(Binding.class).stream()
                .filter(b -> b.getExchange().equals(exchange))
                .collect(Collectors.toMap(Binding::getRoutingKey, Binding::getDestination));
    }
}
