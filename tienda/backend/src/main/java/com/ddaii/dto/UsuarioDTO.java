package com.ddaii.dto;

public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String email;
    private Boolean premium;

    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String nombre, String email, Boolean premium) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.premium = premium;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getPremium() { return premium; }
    public void setPremium(Boolean premium) { this.premium = premium; }
}
