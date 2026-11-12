package com.ddaii.messaging;

import com.ddaii.events.ProductoScrapeado;
import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.events.SuscripcionActivada;
import com.ddaii.services.ProductoService;
import com.ddaii.services.RecomendacionService;
import com.ddaii.services.SuscripcionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidores de colas RabbitMQ (docs/PLAN-INTEGRACION.md 4.2-4.4).
 * ProductoScrapeado -> crea o actualiza producto.
 * SuscripcionActivada -> marca usuario como premium.
 * RecomendacionSolicitada -> procesa y guarda el resultado.
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

    @RabbitListener(queues = "${spring.rabbitmq.queue.producto.scrapeado:producto.scrapeado.queue}")
    public void consumirProductoScrapeado(ProductoScrapeado evento) {
        productoService.upsertDesdeScrapeado(evento);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.suscripcion.activada:suscripcion.activada.queue}")
    public void consumirSuscripcionActivada(SuscripcionActivada evento) {
        // Marca al usuario como premium (patrón Observer). Idempotente: no republica el evento.
        suscripcionService.marcarPremium(evento.getUsuarioId());
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.recomendacion.solicitada:recomendacion.solicitada.queue}")
    public void consumirRecomendacionSolicitada(RecomendacionSolicitada evento) {
        recomendacionService.procesarSolicitud(evento);
    }
}
