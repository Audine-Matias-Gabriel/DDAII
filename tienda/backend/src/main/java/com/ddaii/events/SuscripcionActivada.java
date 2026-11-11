package com.ddaii.events;

import java.time.LocalDateTime;

/**
 * Evento SuscripcionActivada: lo publica el backend al aprobarse el pago
 * y lo consume el backend para marcar premium (docs/PLAN-INTEGRACION.md 4.3).
 */
public class SuscripcionActivada {

    private String tipo = "SuscripcionActivada";
    private Long usuarioId;
    private Long suscripcionId;
    private String idPago;
    private String estado = "ACTIVA";
    private LocalDateTime fecha = LocalDateTime.now();

    public SuscripcionActivada() {}

    public SuscripcionActivada(Long usuarioId, Long suscripcionId, String idPago) {
        this.usuarioId = usuarioId;
        this.suscripcionId = suscripcionId;
        this.idPago = idPago;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getSuscripcionId() { return suscripcionId; }
    public void setSuscripcionId(Long suscripcionId) { this.suscripcionId = suscripcionId; }
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
