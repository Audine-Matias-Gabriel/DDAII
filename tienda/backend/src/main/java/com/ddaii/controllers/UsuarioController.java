package com.ddaii.controllers;

import com.ddaii.domain.Usuario;
import com.ddaii.dto.UsuarioDTO;
import com.ddaii.dto.UsuarioRequest;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.repositories.UsuarioRepository;
import com.ddaii.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint de usuarios (legado). El registro principal se hace por /api/auth/registro.
 *
 * - POST crea el usuario con las mismas validaciones que el registro (antes aceptaba sin
 *   contraseña y guardaba "default", y con un email existente devolvía los datos de ese usuario).
 * - GET {id} devuelve solo el usuario de la sesión: otro id responde 404, para no exponer emails ajenos.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public UsuarioController(UsuarioRepository usuarioRepository, AuthService authService) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@RequestBody UsuarioRequest request) {
        var registrado = authService.registrar(request.getNombre(), request.getEmail(), request.getPassword());
        var u = registrado.getUsuario();
        return ResponseEntity.ok(new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getPremium()));
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Long id, @AuthenticationPrincipal String usuarioIdStr) {
        if (usuarioIdStr == null || !String.valueOf(id).equals(usuarioIdStr)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado: " + id);
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getPremium());
    }
}
