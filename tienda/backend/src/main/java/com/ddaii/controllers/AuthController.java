package com.ddaii.controllers;

import com.ddaii.dto.AuthResponse;
import com.ddaii.domain.Usuario;
import com.ddaii.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registro de cuenta. Cuerpo: { nombre, email, password }
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegistroRequest request) {
        AuthResponse response = authService.registrar(
                request.getNombre(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Login. Cuerpo: { email, password }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Logout. Header: Authorization: Bearer <token>
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = extraerToken(authorization);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Datos del usuario autenticado. Header: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> me(@RequestHeader("Authorization") String authorization) {
        String token = extraerToken(authorization);
        Usuario usuario = authService.obtenerUsuario(token);
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new UsuarioDTO(usuario));
    }

    private String extraerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token de autorización requerido");
        }
        return authorization.substring(7);
    }

    // ---- DTOs internos de request ----

    public static class RegistroRequest {
        private String nombre;
        private String email;
        private String password;

        public RegistroRequest() {}

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ---- DTO de respuesta de usuario ----

    public static class UsuarioDTO {
        private Long id;
        private String nombre;
        private String email;
        private Boolean premium;

        public UsuarioDTO() {}

        public UsuarioDTO(Usuario usuario) {
            this.id = usuario.getId();
            this.nombre = usuario.getNombre();
            this.email = usuario.getEmail();
            this.premium = usuario.getPremium();
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
