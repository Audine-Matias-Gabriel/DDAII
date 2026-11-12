package com.ddaii.dto;

/**
 * DTO de un producto marcado como favorito.
 */
public class FavoritoDTO {

    private Long productoId;
    private String nombre;
    private Double precio;
    private String imagenUrl;
    private Boolean disponible;

    public FavoritoDTO() {}

    public FavoritoDTO(Long productoId, String nombre, Double precio,
                       String imagenUrl, Boolean disponible) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}
