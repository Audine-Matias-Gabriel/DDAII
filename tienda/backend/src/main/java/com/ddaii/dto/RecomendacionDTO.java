package com.ddaii.dto;

/**
 * Recomendación por contenido (docs/PLAN-INTEGRACION.md 2.5).
 */
public class RecomendacionDTO {

    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private String imagenUrl;
    private double puntaje;

    public RecomendacionDTO() {}

    public RecomendacionDTO(Long id, String nombre, String categoria, Double precio,
                            String imagenUrl, double puntaje) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.puntaje = puntaje;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public double getPuntaje() { return puntaje; }
    public void setPuntaje(double puntaje) { this.puntaje = puntaje; }
}
