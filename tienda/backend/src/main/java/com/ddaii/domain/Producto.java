package com.ddaii.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Producto {
    private long id;
    private String nombre;
    private  String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Categoria categoria;
    private long tiendaId;
    private String imageUrl;
}
