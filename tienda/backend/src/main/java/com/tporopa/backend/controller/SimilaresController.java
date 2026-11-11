package com.tporopa.backend.controller;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.service.ProductoService;
import com.tporopa.backend.service.XimilarFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Prendas similares mediante Ximilar (P3, Día 6).
 * GET /api/productos/{id}/similares devuelve prendas de la otra tienda.
 */
@RestController
@RequestMapping("/api/productos")
public class SimilaresController {

    private final ProductoService productoService;
    private final XimilarFacade ximilarFacade;

    public SimilaresController(ProductoService productoService, XimilarFacade ximilarFacade) {
        this.productoService = productoService;
        this.ximilarFacade = ximilarFacade;
    }

    @GetMapping("/{id}/similares")
    public List<String> similares(@PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        String imagenUrl = producto.getImagenUrl() != null ? producto.getImagenUrl() : "";
        return ximilarFacade.buscarSimilares(imagenUrl, 5);
    }
}
