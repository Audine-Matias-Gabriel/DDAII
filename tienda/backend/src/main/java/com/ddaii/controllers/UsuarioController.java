package com.ddaii.controllers;

import com.ddaii.domain.Usuario;
import com.ddaii.dto.UsuarioDTO;
import com.ddaii.dto.UsuarioRequest;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint de usuarios (legado). El registro principal se hace por /api/auth/registro.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioRequest request) {
        String password = request.getPassword() != null ? request.getPassword() : "default";
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail());
        if (usuario == null) {
            usuario = usuarioRepository.save(
                    new Usuario(request.getNombre(), request.getEmail(),
                            passwordEncoder.encode(password)));
        }
        return ResponseEntity.ok(new UsuarioDTO(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getPremium()));
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getPremium());
    }
}
