package com.ddaii.dto;

/** Producto del catálogo encontrado a partir de una imagen, con su distancia de Ximilar. */
public class ResultadoVisualDTO {

    private ProductoDTO producto;
    /** Distancia devuelta por Ximilar (menor = más parecido); null si la API no la informó. */
    private Double distancia;

    public ResultadoVisualDTO() {}

    public ResultadoVisualDTO(ProductoDTO producto, Double distancia) {
        this.producto = producto;
        this.distancia = distancia;
    }

    public ProductoDTO getProducto() { return producto; }
    public void setProducto(ProductoDTO producto) { this.producto = producto; }
    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }
}
