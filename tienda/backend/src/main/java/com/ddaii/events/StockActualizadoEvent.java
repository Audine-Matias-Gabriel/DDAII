package com.ddaii.events;

import java.time.LocalDateTime;

public record StockActualizadoEvent(
    Long pedidoId,
    Long productoId,
    String nombreProducto,
    int cantidadVendida,
    int stockAnterior,
    int stockActual,
    LocalDateTime fecha
) { }
