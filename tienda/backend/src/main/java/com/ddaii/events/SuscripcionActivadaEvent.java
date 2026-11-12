package com.ddaii.events;

import java.time.LocalDateTime;

/**
 * El pago del plan premium fue aprobado. Publica y consume el backend (P5).
 * Contrato en docs/eventos.md.
 */
public record SuscripcionActivadaEvent(
        Long suscripcionId,
        Long usuarioId,
        String pagoId,
        LocalDateTime fecha
) {}
