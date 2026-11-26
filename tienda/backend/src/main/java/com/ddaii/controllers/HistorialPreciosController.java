package com.ddaii.controllers;

import com.ddaii.dto.PrecioHistorialDTO;
import com.ddaii.services.HistorialPreciosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Serie temporal de precios del catálogo (snapshots diarios del scraper).
 * GET /api/productos/{id}/historial-precios
 */
@RestController
@RequestMapping("/api/productos")
public class HistorialPreciosController {

    private final HistorialPreciosService historialPreciosService;

    public HistorialPreciosController(HistorialPreciosService historialPreciosService) {
        this.historialPreciosService = historialPreciosService;
    }

    @GetMapping("/{id}/historial-precios")
    public List<PrecioHistorialDTO> historial(@PathVariable Long id) {
        return historialPreciosService.historial(id);
    }
}
