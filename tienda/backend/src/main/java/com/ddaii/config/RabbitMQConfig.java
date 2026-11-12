package com.ddaii.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Topología de RabbitMQ del sistema. El contrato de cada evento está en
 * docs/eventos.md: si cambia algo acá, se actualiza ahí en el mismo PR.
 *
 * Todos los eventos se publican en un único exchange topic. Cada cola tiene
 * su cola de mensajes muertos (DLQ): un mensaje que el consumidor rechaza
 * sin reencolar va al exchange de errores (DLX) y de ahí a la DLQ de su cola.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "webderopa.eventos";
    public static final String DLX = "webderopa.dlx";

    public static final String RK_PRODUCTO_SCRAPEADO = "producto.scrapeado";
    public static final String RK_SUSCRIPCION_ACTIVADA = "suscripcion.activada";
    public static final String RK_RECOMENDACION_SOLICITADA = "recomendacion.solicitada";

    public static final String COLA_PRODUCTO_SCRAPEADO = "backend.producto-scrapeado";
    public static final String COLA_SUSCRIPCION_ACTIVADA = "backend.suscripcion-activada";
    public static final String COLA_RECOMENDACION_SOLICITADA = "backend.recomendacion-solicitada";

    public static final String SUFIJO_DLQ = ".dlq";

    // --- Prototipo de pedidos (temporal) -----------------------------------
    // Lo siguen usando PedidoCreadoEventListener y PedidoCreadoConsumer. Se
    // borra junto con ellos en el punto e) del plan de eventos.

    /** Routing key con la que se publica el pedido creado. */
    public static final String RK_PEDIDO_CREADO = "pedido.creado";

    /**
     * Cola donde llega el evento de pedido creado, enlazada al exchange de
     * eventos con {@link #RK_PEDIDO_CREADO}.
     */
    public static final String COLA_PEDIDO_CREADO = "tienda.pedidos.creado";

    /**
     * Cola de mensajes muertos: recibe los mensajes que el consumidor
     * rechaza y no vuelve a encolar.
     */
    public static final String COLA_PEDIDO_CREADO_DLQ = COLA_PEDIDO_CREADO + SUFIJO_DLQ;

    /**
     * Declara los exchanges, las colas con sus DLQ y los bindings. Spring AMQP
     * los crea en el broker al abrir la primera conexión; si ya existen con
     * los mismos argumentos, no hace nada.
     */
    @Bean
    public Declarables topologiaEventos() {
        TopicExchange eventos = ExchangeBuilder.topicExchange(EXCHANGE)
                .durable(true)
                .build();
        DirectExchange dlx = ExchangeBuilder.directExchange(DLX)
                .durable(true)
                .build();

        List<Declarable> declarables = new ArrayList<>(List.of(eventos, dlx));
        declarables.addAll(cola(COLA_PRODUCTO_SCRAPEADO, RK_PRODUCTO_SCRAPEADO, eventos, dlx));
        declarables.addAll(cola(COLA_SUSCRIPCION_ACTIVADA, RK_SUSCRIPCION_ACTIVADA, eventos, dlx));
        declarables.addAll(cola(COLA_RECOMENDACION_SOLICITADA, RK_RECOMENDACION_SOLICITADA, eventos, dlx));
        return new Declarables(declarables);
    }

    /**
     * Una cola durable enlazada al exchange de eventos con su routing key, y
     * su DLQ enlazada al DLX con el nombre de la DLQ como routing key.
     */
    private static List<Declarable> cola(
            String nombre,
            String routingKey,
            TopicExchange eventos,
            DirectExchange dlx) {

        String nombreDlq = nombre + SUFIJO_DLQ;

        Queue cola = QueueBuilder.durable(nombre)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(nombreDlq)
                .build();
        Queue dlq = QueueBuilder.durable(nombreDlq).build();

        Binding bindingCola = BindingBuilder.bind(cola).to(eventos).with(routingKey);
        Binding bindingDlq = BindingBuilder.bind(dlq).to(dlx).with(nombreDlq);

        return List.of(cola, dlq, bindingCola, bindingDlq);
    }

    @Bean
    public Queue colaPedidoCreado() {
        return QueueBuilder.durable(COLA_PEDIDO_CREADO)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COLA_PEDIDO_CREADO_DLQ)
                .build();
    }

    @Bean
    public Queue colaPedidoCreadoDlq() {
        return QueueBuilder.durable(COLA_PEDIDO_CREADO_DLQ).build();
    }

    @Bean
    public Binding bindingPedidoCreado() {
        return new Binding(
                COLA_PEDIDO_CREADO,
                Binding.DestinationType.QUEUE,
                EXCHANGE,
                RK_PEDIDO_CREADO,
                null
        );
    }
}
