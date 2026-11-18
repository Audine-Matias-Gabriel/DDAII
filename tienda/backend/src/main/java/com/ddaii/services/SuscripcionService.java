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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        if (Boolean.TRUE.equals(usuario.getPremium())) {
            throw new IllegalArgumentException("Ya sos premium: no hace falta volver a pagar.");
        }

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
                s = aprobar(s, pago.idPago());
            } else if ("rejected".equals(pago.estado()) || "cancelled".equals(pago.estado())) {
                s.setEstado("RECHAZADA");
                s = suscripcionRepository.save(s);
            }
        }
        return aDTO(s);
    }

    /**
     * Para cuando el comprador no vuelve por la back URL (en localhost Mercado
     * Pago no redirige solo): busca en Mercado Pago un pago aprobado para cada
     * suscripción PENDIENTE del usuario y la aprueba. Devuelve todas sus
     * suscripciones, de la más nueva a la más vieja.
     */
    @Transactional
    public List<SuscripcionDTO> verificarPagosPendientes(Long usuarioId) {
        List<Suscripcion> suscripciones = suscripcionRepository.findByUsuarioIdOrderByIdDesc(usuarioId);
        List<SuscripcionDTO> resultado = new ArrayList<>();
        for (Suscripcion s : suscripciones) {
            if ("PENDIENTE".equals(s.getEstado())) {
                Optional<PagoConsultado> pago = mercadoPagoGateway.buscarPagoAprobado(s.getId());
                if (pago.isPresent()) {
                    s = aprobar(s, pago.get().idPago());
                }
            }
            resultado.add(aDTO(s));
        }
        return resultado;
    }

    /** Pasa la suscripción a APROBADA y publica SuscripcionActivada (marca premium). */
    private Suscripcion aprobar(Suscripcion s, String idPago) {
        s.setEstado("APROBADA");
        s.setIdPago(idPago);
        s = suscripcionRepository.save(s);
        eventPublisher.publicarSuscripcionActivada(s.getUsuario().getId(), s.getId(), s.getIdPago());
        return s;
    }

    private SuscripcionDTO aDTO(Suscripcion s) {
        return new SuscripcionDTO(s.getId(), s.getUsuario().getId(), s.getEstado(), s.getIdPago(), s.getFecha());
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
