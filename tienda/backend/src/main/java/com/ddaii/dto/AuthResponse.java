package com.ddaii.dto;

import com.ddaii.domain.Usuario;

/**
 * Respuesta de login / registro: token de sesión + datos del usuario.
 */
public class AuthResponse {

    private String token;
    private UsuarioDTO usuario;

    public AuthResponse() {}

    public AuthResponse(String token, com.ddaii.domain.Usuario usuario) {
        this.token = token;
        this.usuario = new UsuarioDTO(usuario);
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO usuario) { this.usuario = usuario; }

    public static class UsuarioDTO {
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
}
