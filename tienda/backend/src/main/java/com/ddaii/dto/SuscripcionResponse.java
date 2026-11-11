package com.ddaii.dto;

/**
 * Respuesta de POST /api/suscripciones (docs/PLAN-INTEGRACION.md 2.7).
 */
public class SuscripcionResponse {

    private Long suscripcionId;
    private String estado;
    private String urlPago;

    public SuscripcionResponse() {}

    public SuscripcionResponse(Long suscripcionId, String estado, String urlPago) {
        this.suscripcionId = suscripcionId;
        this.estado = estado;
        this.urlPago = urlPago;
    }

    public Long getSuscripcionId() { return suscripcionId; }
    public void setSuscripcionId(Long suscripcionId) { this.suscripcionId = suscripcionId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUrlPago() { return urlPago; }
    public void setUrlPago(String urlPago) { this.urlPago = urlPago; }
}
