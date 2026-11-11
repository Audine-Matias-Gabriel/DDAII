package com.tporopa.backend.controller;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de tiendas (P2, Día 3).
 */
@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    private final ProductoService productoService;

    public TiendaController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoDTO> tiendas() {
        return productoService.disponibles();
    }
}
