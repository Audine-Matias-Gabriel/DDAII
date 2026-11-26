package com.ddaii.dto;

import java.time.LocalDate;

/** Un punto de la serie temporal de precios de un producto. */
public class PrecioHistorialDTO {

    private LocalDate fecha;
    private Double precio;

    public PrecioHistorialDTO() {}

    public PrecioHistorialDTO(LocalDate fecha, Double precio) {
        this.fecha = fecha;
        this.precio = precio;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}
