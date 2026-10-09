package com.ddaii.events;

import com.ddaii.domain.Genero;

import java.time.LocalDateTime;

public record StockActualizadoEvent(
    Long pedidoId,
    Long productoId,
    String nombreProducto,
    Genero genero,
    String talle,
    int cantidadVendida,
    int stockAnterior,
    int stockActual,
    LocalDateTime fecha
) { }
