package com.ddaii.events;

import java.time.LocalDateTime;

/**
 * Evento RecomendacionSolicitada (docs/PLAN-INTEGRACION.md 4.4).
 */
public class RecomendacionSolicitada {

    private String tipo = "RecomendacionSolicitada";
    private String solicitudId;
    private Long productoId;
    private int limite;
    private LocalDateTime fecha = LocalDateTime.now();

    public RecomendacionSolicitada() {}

    public RecomendacionSolicitada(String solicitudId, Long productoId, int limite) {
        this.solicitudId = solicitudId;
        this.productoId = productoId;
        this.limite = limite;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public int getLimite() { return limite; }
    public void setLimite(int limite) { this.limite = limite; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
