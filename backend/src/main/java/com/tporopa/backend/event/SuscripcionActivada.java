package com.tporopa.backend.event;

/**
 * Evento publicado por el backend (P5) cuando Mercado Pago aprueba el pago,
 * consumido para marcar al usuario como premium (Observer, Día 6).
 */
public class SuscripcionActivada {

    private Long usuarioId;
    private String estado;
    private String idPago;

    public SuscripcionActivada() {}

    public SuscripcionActivada(Long usuarioId, String estado, String idPago) {
        this.usuarioId = usuarioId;
        this.estado = estado;
        this.idPago = idPago;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }
}
