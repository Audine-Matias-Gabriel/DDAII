package com.ddaii.controllers;

import com.ddaii.dto.TiendaDTO;
import com.ddaii.services.TiendaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de tiendas (docs/PLAN-INTEGRACION.md 2.3).
 */
@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    private final TiendaService tiendaService;

    public TiendaController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @GetMapping
    public List<TiendaDTO> tiendas() {
        return tiendaService.listar();
    }
}
