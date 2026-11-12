package com.ddaii.dto;

public class TiendaDTO {

    private Long id;
    private String nombre;
    private String urlBase;
    private Boolean activa;

    public TiendaDTO() {}

    public TiendaDTO(Long id, String nombre, String urlBase, Boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.urlBase = urlBase;
        this.activa = activa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUrlBase() { return urlBase; }
    public void setUrlBase(String urlBase) { this.urlBase = urlBase; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
