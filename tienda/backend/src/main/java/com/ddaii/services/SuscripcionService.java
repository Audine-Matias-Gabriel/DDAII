package com.ddaii.services;

import com.ddaii.domain.Suscripcion;
import com.ddaii.domain.Usuario;
import com.ddaii.dto.SuscripcionDTO;
import com.ddaii.dto.SuscripcionResponse;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.integrations.MercadoPagoGateway;
import com.ddaii.integrations.MercadoPagoGateway.PagoConsultado;
import com.ddaii.integrations.MercadoPagoGateway.PreferenciaPago;
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
    private final MercadoPagoGateway mercadoPagoGateway;

    public SuscripcionService(UsuarioRepository usuarioRepository,
                              SuscripcionRepository suscripcionRepository,
                              EventPublisher eventPublisher,
                              MercadoPagoGateway mercadoPagoGateway) {
        this.usuarioRepository = usuarioRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.eventPublisher = eventPublisher;
        this.mercadoPagoGateway = mercadoPagoGateway;
    }

    /**
     * Crea la suscripción en estado PENDIENTE y la preferencia de pago en
     * Mercado Pago. La suscripción se guarda primero porque su id viaja a
     * Mercado Pago como referencia. Si Mercado Pago falla, la excepción
     * deshace la transacción y no queda una suscripción huérfana.
     */
    @Transactional
    public SuscripcionResponse crearPagoPremium(Long usuarioId, String plan) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));

        Suscripcion s = suscripcionRepository.save(new Suscripcion(usuario, "PENDIENTE", null));

        PreferenciaPago preferencia = mercadoPagoGateway.crearPreferenciaPremium(s.getId());
        s.setPreferenciaId(preferencia.preferenciaId());
        s = suscripcionRepository.save(s);

        return new SuscripcionResponse(s.getId(), s.getEstado(), preferencia.urlPago());
    }

    /**
     * Confirma una suscripción al volver el comprador de Mercado Pago: consulta
     * el pago a la API y, si está aprobado, pasa la suscripción a APROBADA y
     * publica SuscripcionActivada. Es idempotente: confirmar dos veces no
     * republica el evento.
     */
    @Transactional
    public SuscripcionDTO confirmarPagoMercadoPago(Long usuarioId, Long suscripcionId, Long paymentId) {
        Suscripcion s = suscripcionRepository.findById(suscripcionId)
                .filter(x -> x.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada: " + suscripcionId));

        if (!"APROBADA".equals(s.getEstado())) {
            PagoConsultado pago = mercadoPagoGateway.consultarPago(paymentId);

            if (!String.valueOf(suscripcionId).equals(pago.suscripcionRef())) {
                throw new IllegalArgumentException(
                        "El pago " + paymentId + " no corresponde a la suscripción " + suscripcionId);
            }

            if ("approved".equals(pago.estado())) {
                s.setEstado("APROBADA");
                s.setIdPago(pago.idPago());
                s = suscripcionRepository.save(s);
                eventPublisher.publicarSuscripcionActivada(usuarioId, s.getId(), s.getIdPago());
            } else if ("rejected".equals(pago.estado()) || "cancelled".equals(pago.estado())) {
                s.setEstado("RECHAZADA");
                s = suscripcionRepository.save(s);
            }
        }
        return new SuscripcionDTO(s.getId(), usuarioId, s.getEstado(), s.getIdPago(), s.getFecha());
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
