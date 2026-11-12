package com.ddaii.messaging;

import com.ddaii.events.ProductoScrapeado;
import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.events.SuscripcionActivada;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Publicador de eventos a RabbitMQ (docs/PLAN-INTEGRACION.md 4.1-4.4).
 */
@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.exchange:tienda.exchange}")
    private String exchange;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarProductoScrapeado(ProductoScrapeado evento) {
        rabbitTemplate.convertAndSend(exchange, "producto.scrapeado", evento);
    }

    public void publicarSuscripcionActivada(Long usuarioId, Long suscripcionId, String idPago) {
        SuscripcionActivada evento = new SuscripcionActivada(usuarioId, suscripcionId, idPago);
        rabbitTemplate.convertAndSend(exchange, "suscripcion.activada", evento);
    }

    public void publicarRecomendacionSolicitada(String solicitudId, Long productoId, int limite) {
        RecomendacionSolicitada evento = new RecomendacionSolicitada(solicitudId, productoId, limite);
        rabbitTemplate.convertAndSend(exchange, "recomendacion.solicitada", evento);
    }
}
