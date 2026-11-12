package com.ddaii.services;

import com.ddaii.domain.Suscripcion;
import com.ddaii.domain.Usuario;
import com.ddaii.dto.SuscripcionDTO;
import com.ddaii.dto.SuscripcionResponse;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.messaging.EventPublisher;
import com.ddaii.repositories.SuscripcionRepository;
import com.ddaii.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suscripciones al plan premium con Mercado Pago (docs/PLAN-INTEGRACION.md 2.7-2.9 y 7).
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

    @Transactional
    public SuscripcionResponse crearPagoPremium(Long usuarioId, String plan) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        // Simulación del pago de prueba en sandbox de Mercado Pago.
        String idPago = "MP-TEST-" + System.currentTimeMillis();
        String urlPago = "https://sandbox.mercadopago.com/checkout/v1/preferences?pref_id=" + idPago;

        Suscripcion s = new Suscripcion(usuario, "PENDIENTE", idPago);
        s = suscripcionRepository.save(s);

        return new SuscripcionResponse(s.getId(), s.getEstado(), urlPago);
    }

    /**
     * Webhook de Mercado Pago: consulta el estado y publica SuscripcionActivada.
     */
    @Transactional
    public void confirmarPago(String idPago, boolean aprobado) {
        Suscripcion s = suscripcionRepository.findByIdPago(idPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado: " + idPago));

        if (!aprobado || "APROBADA".equals(s.getEstado())) {
            return;
        }

        s.setEstado("APROBADA");
        suscripcionRepository.save(s);

        eventPublisher.publicarSuscripcionActivada(s.getUsuario().getId(), s.getId(), s.getIdPago());
    }

    /**
     * Marca al usuario como premium de forma idempotente (consumidor Observer).
     */
    @Transactional
    public void marcarPremium(Long usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        if (Boolean.TRUE.equals(u.getPremium())) {
            return;
        }

        u.setPremium(true);
        usuarioRepository.save(u);
    }

    @Transactional(readOnly = true)
    public SuscripcionDTO estado(Long id) {
        Suscripcion s = suscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada: " + id));
        return new SuscripcionDTO(s.getId(), s.getUsuario().getId(), s.getEstado(), s.getIdPago(), s.getFecha());
    }
}
