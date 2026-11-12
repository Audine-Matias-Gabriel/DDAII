package com.ddaii.dto;

/**
 * Respuesta 202 de POST /api/recomendaciones/solicitudes (docs/PLAN-INTEGRACION.md 2.6).
 */
public class RecomendacionSolicitudResponse {

    private String mensaje;
    private String solicitudId;

    public RecomendacionSolicitudResponse() {}

    public RecomendacionSolicitudResponse(String mensaje, String solicitudId) {
        this.mensaje = mensaje;
        this.solicitudId = solicitudId;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
}
