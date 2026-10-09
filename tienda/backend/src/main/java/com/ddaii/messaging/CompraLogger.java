package com.ddaii.messaging;

import com.ddaii.events.StockActualizadoEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CompraLogger {

    private static final Logger log =
            LoggerFactory.getLogger(CompraLogger.class);

    @EventListener
    public void alActualizarStock(
            StockActualizadoEvent evento) {

        log.info("================================");
        log.info("COMPRA REALIZADA");
        log.info("Pedido: {}", evento.pedidoId());
        log.info("Producto: {}", evento.nombreProducto());
        log.info("Género: {}", evento.genero());
        log.info("Talle: {}", evento.talle());
        log.info("Cantidad: {}", evento.cantidadVendida());
        log.info("Stock anterior: {}", evento.stockAnterior());
        log.info("Stock actual: {}", evento.stockActual());
        log.info("================================");
    }
}