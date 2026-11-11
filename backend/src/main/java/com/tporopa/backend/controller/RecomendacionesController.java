package com.tporopa.backend.controller;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recomendaciones por contenido (P4).
 * Día 4: GET /api/recomendaciones?productoId=&limite= (REST, documentado en Swagger).
 * Día 5: POST /api/recomendaciones/solicitudes (cola, responde 202).
 */
@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionesController {

    private final RecomendacionService recomendacionService;

    public RecomendacionesController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping
    public List<ProductoDTO> recomendar(@RequestParam Long productoId,
                                        @RequestParam(required = false) Integer limite) {
        return recomendacionService.recomendar(productoId, limite);
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<Void> solicitarPorCola(@RequestParam Long productoId,
                                                 @RequestParam(required = false) Integer limite) {
        recomendacionService.solicitarPorCola(productoId, limite);
        return ResponseEntity.accepted().build();
    }
}
