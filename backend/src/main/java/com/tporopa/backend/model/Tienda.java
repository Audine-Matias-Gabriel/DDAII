package com.tporopa.backend.model;

import jakarta.persistence.*;

/**
 * Tienda de origen del catálogo (scraper lee de aquí).
 */
@Entity
@Table(name = "tienda")
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String urlOriginal;

    public Tienda() {}

    public Tienda(String nombre, String urlOriginal) {
        this.nombre = nombre;
        this.urlOriginal = urlOriginal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }
}
