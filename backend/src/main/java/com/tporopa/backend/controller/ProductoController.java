package com.tporopa.backend.controller;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints del catálogo (P2, Día 3).
 * GET /api/productos con filtros y paginación
 * GET /api/productos/{id}
 * GET /api/productos/disponibles
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public Page<ProductoDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String tienda,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String texto,
            Pageable pageable) {
        return productoService.filtrar(categoria, tienda, genero, precioMin, precioMax, texto, pageable);
    }

    @GetMapping("/{id}")
    public ProductoDTO porId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/disponibles")
    public List<ProductoDTO> disponibles() {
        return productoService.disponibles();
    }
}
