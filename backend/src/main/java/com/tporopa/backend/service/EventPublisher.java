package com.tporopa.backend.service;

import com.tporopa.backend.event.ProductoScrapeado;
import com.tporopa.backend.event.RecomendacionSolicitada;
import com.tporopa.backend.event.SuscripcionActivada;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publicador genérico de eventos a RabbitMQ (P5, Día 2).
 * Reutiliza las confirmaciones del broker en lugar de PedidoCreado.
 */
@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:webropa.events}")
    private String exchange;

    @Value("${rabbitmq.queue.producto.scrapeado:cola.producto.scrapeado}")
    private String queueProductoScrapeado;

    @Value("${rabbitmq.queue.suscripcion.activada:cola.suscripcion.activada}")
    private String queueSuscripcionActivada;

    @Value("${rabbitmq.queue.recomendacion.solicitada:cola.recomendacion.solicitada}")
    private String queueRecomendacionSolicitada;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarProductoScrapeado(ProductoScrapeado evento) {
        rabbitTemplate.convertAndSend(exchange, "evento.producto.scrapeado", evento);
    }

    public void publicarSuscripcionActivada(Long usuarioId, String idPago) {
        SuscripcionActivada evento = new SuscripcionActivada(usuarioId, "APROBADA", idPago);
        rabbitTemplate.convertAndSend(exchange, "evento.suscripcion.activada", evento);
    }

    public void publicarRecomendacionSolicitada(Long productoId, int limite) {
        RecomendacionSolicitada evento = new RecomendacionSolicitada(productoId, limite);
        rabbitTemplate.convertAndSend(exchange, "evento.recomendacion.solicitada", evento);
    }
}
