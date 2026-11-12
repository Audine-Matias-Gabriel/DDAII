package com.ddaii.controllers;

import com.ddaii.dto.FavoritoDTO;
import com.ddaii.services.FavoritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    /**
     * Lista los favoritos del usuario autenticado.
     * Header: Authorization: Bearer <token>
     */
    @GetMapping
    public ResponseEntity<List<FavoritoDTO>> listar(@AuthenticationPrincipal String usuarioIdStr) {
        Long usuarioId = Long.parseLong(usuarioIdStr);
        List<FavoritoDTO> favoritos = favoritoService.listarFavoritos(usuarioId);
        return ResponseEntity.ok(favoritos);
    }

    /**
     * Agrega un producto a los favoritos del usuario autenticado.
     * Header: Authorization: Bearer <token>
     */
    @PostMapping("/{productoId}")
    public ResponseEntity<Void> agregar(@AuthenticationPrincipal String usuarioIdStr,
                                        @PathVariable Long productoId) {
        Long usuarioId = Long.parseLong(usuarioIdStr);
        favoritoService.agregarFavorito(usuarioId, productoId);
        return ResponseEntity.ok().build();
    }

    /**
     * Quita un producto de los favoritos del usuario autenticado.
     * Header: Authorization: Bearer <token>
     */
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> quitar(@AuthenticationPrincipal String usuarioIdStr,
                                       @PathVariable Long productoId) {
        Long usuarioId = Long.parseLong(usuarioIdStr);
        favoritoService.quitarFavorito(usuarioId, productoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si un producto está en favoritos del usuario autenticado.
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/{productoId}/es-favorito")
    public ResponseEntity<Boolean> esFavorito(@AuthenticationPrincipal String usuarioIdStr,
                                              @PathVariable Long productoId) {
        Long usuarioId = Long.parseLong(usuarioIdStr);
        boolean fav = favoritoService.esFavorito(usuarioId, productoId);
        return ResponseEntity.ok(fav);
    }
}
