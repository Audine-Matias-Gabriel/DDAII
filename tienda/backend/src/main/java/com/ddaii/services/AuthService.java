package com.ddaii.services;

import com.ddaii.domain.Usuario;
import com.ddaii.dto.AuthResponse;
import com.ddaii.repositories.UsuarioRepository;
import com.ddaii.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       TokenService tokenService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registro de un nuevo usuario. La contraseña se guarda hasheada con BCrypt.
     */
    public AuthResponse registrar(String nombre, String email, String password) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        email = email == null ? null : email.trim();
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña tiene que tener al menos 6 caracteres");
        }
        if (usuarioRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("El email ya está registrado: " + email);
        }

        String hash = passwordEncoder.encode(password);
        Usuario usuario = new Usuario(nombre, email, hash);
        usuario = usuarioRepository.save(usuario);

        String token = tokenService.generarToken(usuario);
        return new AuthResponse(token, usuario);
    }

    /**
     * Login: verifica email y contraseña (hash BCrypt) y devuelve un token de sesión.
     */
    public AuthResponse login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenService.generarToken(usuario);
        return new AuthResponse(token, usuario);
    }

    /**
     * Logout: invalida el token de sesión.
     */
    public void logout(String token) {
        tokenService.invalidarToken(token);
    }

    /**
     * Retorna el usuario autenticado a partir del token.
     */
    public Usuario obtenerUsuario(String token) {
        return tokenService.validarToken(token);
    }
}
