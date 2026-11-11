package com.tporopa.backend.listener;

import com.tporopa.backend.event.ProductoScrapeado;
import com.tporopa.backend.event.RecomendacionSolicitada;
import com.tporopa.backend.event.SuscripcionActivada;
import com.tporopa.backend.service.ProductoService;
import com.tporopa.backend.service.RecomendacionService;
import com.tporopa.backend.service.SuscripcionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Consumidores de colas RabbitMQ (P2, P4, P5).
 * Día 5 (P2): ProductoScrapeado -> crea o actualiza producto.
 * Día 6 (P5): SuscripcionActivada -> marca usuario como premium.
 * Día 5 (P4): RecomendacionSolicitada -> procesa y guarda el resultado.
 */
@Component
public class RabbitListeners {

    private final ProductoService productoService;
    private final SuscripcionService suscripcionService;
    private final RecomendacionService recomendacionService;

    public RabbitListeners(ProductoService productoService,
                           SuscripcionService suscripcionService,
                           RecomendacionService recomendacionService) {
        this.productoService = productoService;
        this.suscripcionService = suscripcionService;
        this.recomendacionService = recomendacionService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.producto.scrapeado:cola.producto.scrapeado}")
    public void consumirProductoScrapeado(ProductoScrapeado evento) {
        productoService.upsertDesdeScrapeado(evento);
    }

    @RabbitListener(queues = "${rabbitmq.queue.suscripcion.activada:cola.suscripcion.activada}")
    public void consumirSuscripcionActivada(SuscripcionActivada evento) {
        // Marca al usuario como premium (patrón Observer).
        suscripcionService.confirmarPago(evento.getIdPago(), true);
    }

    @RabbitListener(queues = "${rabbitmq.queue.recomendacion.solicitada:cola.recomendacion.solicitada}")
    public void consumirRecomendacionSolicitada(RecomendacionSolicitada evento) {
        recomendacionService.recomendar(evento.getProductoId(), evento.getLimite());
    }
}
