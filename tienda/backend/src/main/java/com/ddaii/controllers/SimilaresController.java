package com.ddaii.controllers;

import com.ddaii.dto.ProductoSimilarDTO;
import com.ddaii.services.SimilaresService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Prendas similares mediante Ximilar (docs/PLAN-INTEGRACION.md 2.4 y 6).
 * GET /api/productos/{id}/similares devuelve prendas de otras tiendas.
 */
@RestController
@RequestMapping("/api/productos")
public class SimilaresController {

    private final SimilaresService similaresService;

    public SimilaresController(SimilaresService similaresService) {
        this.similaresService = similaresService;
    }

    @GetMapping("/{id}/similares")
    public List<ProductoSimilarDTO> similares(@PathVariable Long id,
                                              @RequestParam(defaultValue = "5") int limite) {
        return similaresService.buscarSimilares(id, limite);
    }
}
