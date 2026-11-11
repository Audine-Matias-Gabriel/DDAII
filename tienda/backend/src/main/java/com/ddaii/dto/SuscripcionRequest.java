package com.ddaii.dto;

/**
 * Payload de alta de suscripción al plan premium (docs/PLAN-INTEGRACION.md 2.7).
 */
public class SuscripcionRequest {

    private Long usuarioId;
    private String plan = "PREMIUM";

    public SuscripcionRequest() {}

    public SuscripcionRequest(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public SuscripcionRequest(Long usuarioId, String plan) {
        this.usuarioId = usuarioId;
        this.plan = plan;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
}
