package com.tporopa.backend.service;

import com.tporopa.backend.dto.SuscripcionRequest;
import com.tporopa.backend.event.SuscripcionActivada;
import com.tporopa.backend.model.Suscripcion;
import com.tporopa.backend.model.Usuario;
import com.tporopa.backend.repository.SuscripcionRepository;
import com.tporopa.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suscripciones al plan premium con Mercado Pago (P5).
 * Día 4: POST /api/suscripciones crea el pago en sandbox y devuelve la URL de pago.
 * Día 5: webhook confirma el pago y publica SuscripcionActivada.
 * Día 6: consumidor marca al usuario como premium (Observer).
 */
@Service
public class SuscripcionService {

    private final UsuarioRepository usuarioRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final EventPublisher eventPublisher;

    public SuscripcionService(UsuarioRepository usuarioRepository,
                              SuscripcionRepository suscripcionRepository,
                              EventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Crea el pago del plan premium en el sandbox de Mercado Pago.
     * Devuelve la URL de pago (pantalla de prueba).
     */
    @Transactional
    public String crearPagoPremium(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        // Simulación del pago de prueba en sandbox de Mercado Pago.
        // En la entrega real se llama a la API de Mercado Pago y se usa la URL de init_point.
        String idPago = "MP-TEST-" + System.currentTimeMillis();
        String urlPago = "https://sandbox.mercadopago.com/checkout/v1/preferences?pref_id=" + idPago;

        Suscripcion s = new Suscripcion(usuario, "PENDIENTE", idPago);
        suscripcionRepository.save(s);

        return urlPago;
    }

    /**
     * Webhook de Mercado Pago: consulta el estado del pago y, si fue aprobado,
     * publica SuscripcionActivada y marca al usuario como premium.
     * Plan B (Día 5): al volver a /pago/exito el backend consulta el estado del pago.
     */
    @Transactional
    public void confirmarPago(String idPago, boolean aprobado) {
        Suscripcion s = suscripcionRepository.findByIdPago(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + idPago));

        if (aprobado) {
            s.setEstado("APROBADA");
            suscripcionRepository.save(s);

            Usuario u = s.getUsuario();
            u.setPremium(true);
            usuarioRepository.save(u);

            eventPublisher.publicarSuscripcionActivada(u.getId(), s.getIdPago());
        }
    }

    public SuscripcionRequest buildRequest(Long usuarioId) {
        return new SuscripcionRequest(usuarioId);
    }
}
