package com.ddaii.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado persistido de una solicitud asincrónica de recomendaciones
 * (docs/PLAN-INTEGRACION.md 2.6 y 4.4).
 */
@Entity
@Table(name = "recomendacion")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String solicitudId;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ElementCollection
    @CollectionTable(name = "recomendacion_producto", joinColumns = @JoinColumn(name = "recomendacion_id"))
    @Column(name = "producto_recomendado_id")
    private List<Long> productosRecomendados = new ArrayList<>();

    public Recomendacion() {}

    public Recomendacion(String solicitudId, Long productoId) {
        this.solicitudId = solicitudId;
        this.productoId = productoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public List<Long> getProductosRecomendados() { return productosRecomendados; }
    public void setProductosRecomendados(List<Long> productosRecomendados) { this.productosRecomendados = productosRecomendados; }
}
