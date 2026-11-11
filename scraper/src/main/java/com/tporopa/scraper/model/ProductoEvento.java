package com.tporopa.scraper.model;

import java.util.List;

/**
 * DTO del evento ProductoScrapeado que publica el scraper.
 * Debe ser compatible con el evento que consume el backend (com.tporopa.backend.event.ProductoScrapeado).
 */
public class ProductoEvento {

    private String tienda;
    private String idExterno;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String categoria;
    private String genero;
    private String marca;
    private String urlOriginal;
    private String imagenUrl;
    private Boolean disponible;
    private List<String> tallesDisponibles;

    public ProductoEvento() {}

    public ProductoEvento(String tienda, String idExterno, String nombre, String descripcion,
                          Double precio, String categoria, String genero, String marca,
                          String urlOriginal, String imagenUrl, Boolean disponible,
                          List<String> tallesDisponibles) {
        this.tienda = tienda;
        this.idExterno = idExterno;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.genero = genero;
        this.marca = marca;
        this.urlOriginal = urlOriginal;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
        this.tallesDisponibles = tallesDisponibles;
    }

    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }
    public String getIdExterno() { return idExterno; }
    public void setIdExterno(String idExterno) { this.idExterno = idExterno; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public List<String> getTallesDisponibles() { return tallesDisponibles; }
    public void setTallesDisponibles(List<String> tallesDisponibles) { this.tallesDisponibles = tallesDisponibles; }
}
