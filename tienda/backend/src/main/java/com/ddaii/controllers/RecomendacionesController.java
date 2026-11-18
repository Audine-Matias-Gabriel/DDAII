package com.ddaii.controllers;

import com.ddaii.dto.RecomendacionDTO;
import com.ddaii.dto.RecomendacionSolicitudRequest;
import com.ddaii.dto.RecomendacionSolicitudResponse;
import com.ddaii.services.RecomendacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Recomendaciones por contenido (docs/PLAN-INTEGRACION.md 2.5-2.6).
 * GET  /api/recomendaciones?productoId=&limite= (sincrónico)
 * POST /api/recomendaciones/solicitudes (cola, responde 202)
 */
@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionesController {

    private final RecomendacionService recomendacionService;

    public RecomendacionesController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    /**
     * estrategia = contenido (por defecto: prendas parecidas) o complementaria (arma un conjunto:
     * arriba, abajo y calzado). limite entre 1 y 20 (5 por defecto).
     */
    @GetMapping
    public List<RecomendacionDTO> recomendar(@RequestParam Long productoId,
                                             @RequestParam(required = false) Integer limite,
                                             @RequestParam(required = false, defaultValue = "contenido") String estrategia) {
        return recomendacionService.recomendar(productoId, limite, estrategia);
    }

    /** Resultado que guardó el consumidor de la cola para una solicitud (404 mientras no se procesó). */
    @GetMapping("/solicitudes/{solicitudId}")
    public Map<String, Object> resultado(@PathVariable String solicitudId) {
        return recomendacionService.resultadoDeSolicitud(solicitudId);
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<RecomendacionSolicitudResponse> solicitarPorCola(
            @Valid @RequestBody RecomendacionSolicitudRequest request) {
        String solicitudId = recomendacionService.solicitarPorCola(
                request.getProductoId(), request.getLimite());
        return ResponseEntity.accepted().body(
                new RecomendacionSolicitudResponse("Solicitud de recomendación recibida", solicitudId));
    }
}
