package com.ddaii.controllers;

import com.ddaii.domain.Usuario;
import com.ddaii.dto.UsuarioDTO;
import com.ddaii.dto.UsuarioRequest;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Login simple sin autenticación real (docs/PLAN-INTEGRACION.md 8.1 y 11).
 * El frontend guarda el id y lo envía en el header X-Usuario-Id.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail());
        if (usuario == null) {
            usuario = usuarioRepository.save(new Usuario(request.getNombre(), request.getEmail()));
        }
        return ResponseEntity.ok(new UsuarioDTO(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getPremium()));
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new com.ddaii.exceptions.RecursoNoEncontradoException(
                        "Usuario no encontrado: " + id));
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getPremium());
    }
}
