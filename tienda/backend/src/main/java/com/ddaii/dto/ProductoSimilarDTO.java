package com.ddaii.dto;

/**
 * Prenda similar de otra tienda (docs/PLAN-INTEGRACION.md 2.4).
 */
public class ProductoSimilarDTO {

    private Long id;
    private String nombre;
    private TiendaDTO tienda;
    private String imagenUrl;
    private String urlOriginal;
    private Double precio;

    public ProductoSimilarDTO() {}

    public ProductoSimilarDTO(Long id, String nombre, TiendaDTO tienda,
                              String imagenUrl, String urlOriginal, Double precio) {
        this.id = id;
        this.nombre = nombre;
        this.tienda = tienda;
        this.imagenUrl = imagenUrl;
        this.urlOriginal = urlOriginal;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TiendaDTO getTienda() { return tienda; }
    public void setTienda(TiendaDTO tienda) { this.tienda = tienda; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}
