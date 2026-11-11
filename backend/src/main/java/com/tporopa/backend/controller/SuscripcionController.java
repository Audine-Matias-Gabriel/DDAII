package com.tporopa.backend.controller;

import com.tporopa.backend.dto.SuscripcionRequest;
import com.tporopa.backend.service.SuscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Suscripciones al plan premium con Mercado Pago (P5).
 * Día 4: POST /api/suscripciones crea el pago en sandbox y devuelve la URL de pago.
 * Día 5: POST /api/suscripciones/webhook (simulación) confirma el pago.
 */
@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> crear(@RequestBody SuscripcionRequest request) {
        String urlPago = suscripcionService.crearPagoPremium(request.getUsuarioId());
        return ResponseEntity.ok(Map.of("urlPago", urlPago));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam String idPago,
                                        @RequestParam(defaultValue = "true") boolean aprobado) {
        suscripcionService.confirmarPago(idPago, aprobado);
        return ResponseEntity.ok().build();
    }
}
