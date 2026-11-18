package com.ddaii.controllers;

import com.ddaii.dto.SuscripcionDTO;
import com.ddaii.dto.SuscripcionResponse;
import com.ddaii.integrations.MercadoPagoFirma;
import com.ddaii.integrations.MercadoPagoGateway;
import com.ddaii.services.SuscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Suscripciones al plan premium con Mercado Pago (docs/PLAN-INTEGRACION.md 2.7-2.9).
 * POST /api/suscripciones crea el pago en sandbox y devuelve la URL de pago.
 * El usuario se resuelve desde el token de sesión (Authorization: Bearer).
 * POST /api/suscripciones/webhook recibe las notificaciones de Mercado Pago (público; consulta el pago a la API).
 * POST /api/suscripciones/{id}/confirmar?paymentId= confirma el pago real consultando a Mercado Pago.
 * POST /api/suscripciones/verificar busca en Mercado Pago los pagos de las suscripciones pendientes del usuario.
 * GET  /api/suscripciones/{id}/estado consulta el estado.
 */
@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final MercadoPagoFirma mercadoPagoFirma;

    public SuscripcionController(SuscripcionService suscripcionService,
                                 MercadoPagoGateway mercadoPagoGateway,
                                 MercadoPagoFirma mercadoPagoFirma) {
        this.suscripcionService = suscripcionService;
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.mercadoPagoFirma = mercadoPagoFirma;
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

    /**
     * Webhook de Mercado Pago (público: lo llama Mercado Pago, sin sesión).
     * Acepta el formato actual ({@code ?type=payment&data.id=123} o cuerpo {@code {"type":"payment","data":{"id":"123"}}})
     * y el IPN viejo ({@code ?topic=payment&id=123}). Solo usa el id: el pago se consulta a la API de
     * Mercado Pago antes de aprobar nada. Responde 200 rápido (Mercado Pago reintenta si no recibe 2xx).
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestParam Map<String, String> params,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "x-signature", required = false) String firma,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {

        String tipo = primero(params.get("type"), params.get("topic"), body != null ? texto(body.get("type")) : null);
        String id = primero(params.get("data.id"), params.get("id"), idDelCuerpo(body));

        if (!"payment".equalsIgnoreCase(tipo) || id == null) {
            return ResponseEntity.ok(Map.of("procesado", false, "motivo", "notificación que no es de un pago"));
        }
        if (!mercadoPagoFirma.esValida(firma, requestId, id)) {
            return ResponseEntity.status(401).body(Map.of("procesado", false, "motivo", "firma inválida"));
        }
        long paymentId;
        try {
            paymentId = Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El id de pago no es válido: " + id);
        }

        var estado = suscripcionService.procesarNotificacionPago(paymentId);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("procesado", estado.isPresent());
        respuesta.put("pago", paymentId);
        respuesta.put("estadoSuscripcion", estado.orElse(null));
        return ResponseEntity.ok(respuesta);
    }

    private static String texto(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private static String idDelCuerpo(Map<String, Object> body) {
        if (body == null) return null;
        Object data = body.get("data");
        if (data instanceof Map<?, ?> m) return texto(((Map<String, Object>) m).get("id"));
        return texto(body.get("id"));
    }

    private static String primero(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    @GetMapping("/{id}/estado")
    public SuscripcionDTO estado(@AuthenticationPrincipal String usuarioIdStr, @PathVariable Long id) {
        return suscripcionService.estado(Long.parseLong(usuarioIdStr), id);
    }
}
