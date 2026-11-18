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
     * Webhook de Mercado Pago: llega solo el id del pago. Se consulta el pago a la API
     * (no se confía en la notificación), se busca la suscripción por external_reference y,
     * si está aprobado, se aprueba y se publica SuscripcionActivada. Idempotente: Mercado
     * Pago reintenta las notificaciones y una suscripción ya aprobada no republica el evento.
     *
     * @return el estado en que quedó la suscripción, o vacío si el pago no es de una suscripción nuestra.
     */
    @Transactional
    public Optional<String> procesarNotificacionPago(Long paymentId) {
        PagoConsultado pago = mercadoPagoGateway.consultarPago(paymentId);
        Long suscripcionId;
        try {
            suscripcionId = Long.parseLong(String.valueOf(pago.suscripcionRef()).trim());
        } catch (NumberFormatException e) {
            return Optional.empty(); // pago de otra cosa (sin external_reference nuestro)
        }
        Optional<Suscripcion> encontrada = suscripcionRepository.findById(suscripcionId);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Suscripcion s = encontrada.get();
        if ("APROBADA".equals(s.getEstado())) {
            return Optional.of(s.getEstado());
        }
        // Solo se aprueba: un rechazo no cierra la suscripción porque el comprador puede
        // reintentar con otra tarjeta en el mismo checkout (llega otra notificación).
        if ("approved".equals(pago.estado())) {
            s = aprobar(s, pago.idPago());
        }
        return Optional.of(s.getEstado());
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

    /** Estado de una suscripción del usuario de la sesión (una ajena responde 404). */
    @Transactional(readOnly = true)
    public SuscripcionDTO estado(Long usuarioId, Long id) {
        Suscripcion s = suscripcionRepository.findById(id)
                .filter(x -> x.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción no encontrada: " + id));
        return new SuscripcionDTO(s.getId(), s.getUsuario().getId(), s.getEstado(), s.getIdPago(), s.getFecha());
    }
}
