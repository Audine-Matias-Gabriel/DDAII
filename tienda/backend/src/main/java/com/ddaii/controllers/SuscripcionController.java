package com.ddaii.controllers;

import com.ddaii.dto.SuscripcionDTO;
import com.ddaii.dto.SuscripcionResponse;
import com.ddaii.integrations.MercadoPagoGateway;
import com.ddaii.services.SuscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Suscripciones al plan premium con Mercado Pago (docs/PLAN-INTEGRACION.md 2.7-2.9).
 * POST /api/suscripciones crea el pago en sandbox y devuelve la URL de pago.
 * El usuario se resuelve desde el token de sesión (Authorization: Bearer).
 * POST /api/suscripciones/webhook confirma el pago (simulación de Mercado Pago).
 * POST /api/suscripciones/{id}/confirmar?paymentId= confirma el pago real consultando a Mercado Pago.
 * POST /api/suscripciones/verificar busca en Mercado Pago los pagos de las suscripciones pendientes del usuario.
 * GET  /api/suscripciones/{id}/estado consulta el estado.
 */
@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final MercadoPagoGateway mercadoPagoGateway;

    public SuscripcionController(SuscripcionService suscripcionService,
                                 MercadoPagoGateway mercadoPagoGateway) {
        this.suscripcionService = suscripcionService;
        this.mercadoPagoGateway = mercadoPagoGateway;
    }

    /** Datos del plan premium para mostrar en Planes: el mismo precio que se cobra en Mercado Pago. Público. */
    @GetMapping("/plan")
    public Map<String, Object> plan() {
        return Map.of(
                "nombre", "PREMIUM",
                "precio", mercadoPagoGateway.precioPremium(),
                "moneda", "ARS");
    }

    @PostMapping
    public ResponseEntity<SuscripcionResponse> crear(@AuthenticationPrincipal String usuarioIdStr) {
        Long usuarioId = Long.parseLong(usuarioIdStr);
        return ResponseEntity.ok(suscripcionService.crearPagoPremium(usuarioId, "PREMIUM"));
    }

    /** El frontend lo llama desde /pago/exito con el payment_id que trae la URL de retorno. */
    @PostMapping("/{id}/confirmar")
    public SuscripcionDTO confirmar(@AuthenticationPrincipal String usuarioIdStr,
                                    @PathVariable Long id,
                                    @RequestParam Long paymentId) {
        return suscripcionService.confirmarPagoMercadoPago(Long.parseLong(usuarioIdStr), id, paymentId);
    }

    /**
     * Busca en Mercado Pago pagos aprobados de las suscripciones pendientes del
     * usuario (cuando no volvió por /pago/exito) y devuelve sus suscripciones.
     */
    @PostMapping("/verificar")
    public List<SuscripcionDTO> verificar(@AuthenticationPrincipal String usuarioIdStr) {
        return suscripcionService.verificarPagosPendientes(Long.parseLong(usuarioIdStr));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam String idPago,
                                        @RequestParam(defaultValue = "true") boolean aprobado) {
        suscripcionService.confirmarPago(idPago, aprobado);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/estado")
    public SuscripcionDTO estado(@PathVariable Long id) {
        return suscripcionService.estado(id);
    }
}
