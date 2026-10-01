package com.ddaii.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    private Long id;

    private String nombre;

    private String descripcion;

    private BigDecimal precio;

    private Integer stock;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    private Long tiendaId;

    private String imagenUrl;

    public Producto() {
    }

    public Producto(
            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Categoria categoria,
            Long tiendaId,
            String imagenUrl) {

        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.tiendaId = tiendaId;
        this.imagenUrl = imagenUrl;
    }

    public boolean estaDisponible() {
        return stock != null && stock > 0;
    }

    /**
     * Descuenta del stock la cantidad indicada.
     */
    public void actualizarStock(Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a 0");
        }

        if (stock == null || cantidad > stock) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto " + id);
        }

        stock -= cantidad;
    }
}