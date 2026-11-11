package com.ddaii.dto;

import java.time.LocalDateTime;

/**
 * Estado de una suscripción (docs/PLAN-INTEGRACION.md 2.9).
 */
public class SuscripcionDTO {

    private Long id;
    private Long usuarioId;
    private String estado;
    private String idPago;
    private LocalDateTime fecha;

    public SuscripcionDTO() {}

    public SuscripcionDTO(Long id, Long usuarioId, String estado, String idPago, LocalDateTime fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.estado = estado;
        this.idPago = idPago;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
