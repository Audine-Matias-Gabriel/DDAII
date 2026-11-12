package com.ddaii.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Payload de POST /api/recomendaciones/solicitudes (docs/PLAN-INTEGRACION.md 2.6).
 */
public class RecomendacionSolicitudRequest {

    @NotNull
    private Long productoId;

    private Integer limite = 5;

    public RecomendacionSolicitudRequest() {}

    public RecomendacionSolicitudRequest(Long productoId, Integer limite) {
        this.productoId = productoId;
        this.limite = limite;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Integer getLimite() { return limite; }
    public void setLimite(Integer limite) { this.limite = limite; }
}
