package com.ddaii.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Precio de un producto en un día determinado.
 * Se graba cada vez que el scraper actualiza el producto (un registro por día).
 */
@Entity
@Table(name = "producto_snapshot", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"producto_id", "fecha"})
})
public class ProductoSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Double precio;

    public ProductoSnapshot() {}

    public ProductoSnapshot(Producto producto, LocalDate fecha, Double precio) {
        this.producto = producto;
        this.fecha = fecha;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}
