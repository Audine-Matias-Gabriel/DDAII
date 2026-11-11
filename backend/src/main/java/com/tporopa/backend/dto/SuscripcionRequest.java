package com.tporopa.backend.dto;

/**
 * Payload de alta de suscripción al plan premium (POST /api/suscripciones).
 */
public class SuscripcionRequest {

    private Long usuarioId;

    public SuscripcionRequest() {}

    public SuscripcionRequest(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}
