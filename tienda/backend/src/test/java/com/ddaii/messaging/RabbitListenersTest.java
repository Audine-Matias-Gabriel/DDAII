package com.ddaii.messaging;

import com.ddaii.events.ProductoScrapeado;
import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.events.SuscripcionActivada;
import com.ddaii.services.ProductoService;
import com.ddaii.services.RecomendacionService;
import com.ddaii.services.SuscripcionService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Cada consumidor delega en su servicio (patrón Observer). */
class RabbitListenersTest {

    private final ProductoService productos = mock(ProductoService.class);
    private final SuscripcionService suscripciones = mock(SuscripcionService.class);
    private final RecomendacionService recomendaciones = mock(RecomendacionService.class);
    private final RabbitListeners listeners = new RabbitListeners(productos, suscripciones, recomendaciones);

    @Test
    void productoScrapeadoHaceUpsert() {
        ProductoScrapeado evento = new ProductoScrapeado();
        listeners.consumirProductoScrapeado(evento);
        verify(productos).upsertDesdeScrapeado(evento);
    }

    @Test
    void suscripcionActivadaMarcaPremium() {
        listeners.consumirSuscripcionActivada(new SuscripcionActivada(4L, 5L, "pago"));
        verify(suscripciones).marcarPremium(4L);
    }

    @Test
    void recomendacionSolicitadaSeProcesa() {
        RecomendacionSolicitada evento = new RecomendacionSolicitada("sol-1", 36L, 3);
        listeners.consumirRecomendacionSolicitada(evento);
        verify(recomendaciones).procesarSolicitud(evento);
    }
}
