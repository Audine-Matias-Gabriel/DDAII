package com.ddaii.controllers;

import com.ddaii.dto.PageResponse;
import com.ddaii.dto.ProductoDTO;
import com.ddaii.services.ProductoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints del catálogo (docs/PLAN-INTEGRACION.md 2.1).
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
    public PageResponse<ProductoDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long tienda,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        if (pagina < 0) throw new IllegalArgumentException("'pagina' no puede ser negativa");
        if (tamano < 1 || tamano > 100) throw new IllegalArgumentException("'tamano' tiene que estar entre 1 y 100");
        // Orden fijo por id: sin ORDER BY, Postgres devuelve las filas en cualquier orden (el
        // scraper las actualiza) y al pedir la página siguiente se repetían o salteaban productos.
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("id"));
        return PageResponse.from(productoService.filtrar(
                categoria, tienda, genero, precioMin, precioMax, texto, pageable));
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
