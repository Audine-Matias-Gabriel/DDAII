package com.tporopa.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de producto acordado el Día 1 (docs/modelo.md).
 * Regla de unicidad: tienda + idExterno.
 */
@Entity
@Table(name = "producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tienda", "id_externo"})
})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_externo", nullable = false)
    private String idExterno;

    @Column(nullable = false)
    private String tienda;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    private String categoria;
    private String genero;
    private String marca;
    private String urlOriginal;
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean disponible = true;

    @ElementCollection
    @CollectionTable(name = "talle_disponible", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "talle")
    private List<String> tallesDisponibles = new ArrayList<>();

    public Producto() {}

    public Producto(String idExterno, String tienda, String nombre, String descripcion,
                    Double precio, String categoria, String genero, String marca,
                    String urlOriginal, String imagenUrl, Boolean disponible) {
        this.idExterno = idExterno;
        this.tienda = tienda;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.genero = genero;
        this.marca = marca;
        this.urlOriginal = urlOriginal;
        this.imagenUrl = imagenUrl;
        this.disponible = disponible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdExterno() { return idExterno; }
    public void setIdExterno(String idExterno) { this.idExterno = idExterno; }
    public String getTienda() { return tienda; }
    public void setTienda(String tienda) { this.tienda = tienda; }
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
