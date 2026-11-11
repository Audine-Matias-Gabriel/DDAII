package com.tporopa.backend.event;

/**
 * Evento publicado por el backend (P4) al llamar POST /api/recomendaciones/solicitudes
 * (responde 202). Un consumidor lo procesa y guarda el resultado (Día 5).
 */
public class RecomendacionSolicitada {

    private Long productoId;
    private int limite;

    public RecomendacionSolicitada() {}

    public RecomendacionSolicitada(Long productoId, int limite) {
        this.productoId = productoId;
        this.limite = limite;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public int getLimite() { return limite; }
    public void setLimite(int limite) { this.limite = limite; }
}
