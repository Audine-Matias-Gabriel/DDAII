package com.ddaii.events;

/**
 * Pedido de recomendaciones de productos parecidos a uno dado. Publica y
 * consume el backend (P4). Contrato en docs/eventos.md.
 */
public record RecomendacionSolicitadaEvent(
        String solicitudId,
        Long productoId,
        int limite
) {}
