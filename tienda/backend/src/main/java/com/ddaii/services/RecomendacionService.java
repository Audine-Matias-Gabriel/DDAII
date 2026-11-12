package com.ddaii.services;

import com.ddaii.domain.Producto;
import com.ddaii.domain.Recomendacion;
import com.ddaii.dto.RecomendacionDTO;
import com.ddaii.events.RecomendacionSolicitada;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import com.ddaii.messaging.EventPublisher;
import com.ddaii.repositories.ProductoRepository;
import com.ddaii.repositories.RecomendacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente de IA de recomendaciones por contenido (docs/ia.md y PLAN 2.5-2.6, 4.4).
 */
@Service
public class RecomendacionService {

    private final ProductoRepository productoRepository;
    private final EventPublisher eventPublisher;
    private final RecomendacionRepository recomendacionRepository;

    public RecomendacionService(ProductoRepository productoRepository,
                                EventPublisher eventPublisher,
                                RecomendacionRepository recomendacionRepository) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
        this.recomendacionRepository = recomendacionRepository;
    }

    @Transactional(readOnly = true)
    public List<RecomendacionDTO> recomendar(Long productoId, Integer limite) {
        Producto entrada = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));

        List<Producto> candidatos = productoRepository.findByDisponibleTrue().stream()
                .filter(p -> !p.getId().equals(entrada.getId()))
                .collect(Collectors.toList());

        EstrategiaRecomendacion estrategia = RecomendacionFactory.crear("contenido");
        return estrategia.recomendar(entrada, candidatos, limite != null ? limite : 5);
    }

    /**
     * Publica la solicitud y responde 202. El consumidor procesa y guarda el resultado.
     */
    @Transactional
    public String solicitarPorCola(Long productoId, Integer limite) {
        String solicitudId = UUID.randomUUID().toString();
        eventPublisher.publicarRecomendacionSolicitada(solicitudId, productoId, limite != null ? limite : 5);
        return solicitudId;
    }

    /**
     * Consumidor de RecomendacionSolicitada: calcula y persiste el resultado (idempotente).
     */
    @Transactional
    public void procesarSolicitud(RecomendacionSolicitada evento) {
        if (recomendacionRepository.findBySolicitudId(evento.getSolicitudId()).isPresent()) {
            return;
        }

        List<RecomendacionDTO> resultado = recomendar(evento.getProductoId(), evento.getLimite());
        Recomendacion r = new Recomendacion(evento.getSolicitudId(), evento.getProductoId());
        r.setProductosRecomendados(resultado.stream().map(RecomendacionDTO::getId).toList());
        recomendacionRepository.save(r);
    }

    public static double puntaje(Producto entrada, Producto candidata) {
        double score = 0.0;
        if (entrada.getCategoria() != null && entrada.getCategoria().equalsIgnoreCase(candidata.getCategoria())) score += 0.4;
        if (entrada.getGenero() != null && entrada.getGenero().equalsIgnoreCase(candidata.getGenero())) score += 0.2;
        if (entrada.getMarca() != null && entrada.getMarca().equalsIgnoreCase(candidata.getMarca())) score += 0.2;
        if (entrada.getPrecio() != null && candidata.getPrecio() != null) {
            double diff = Math.abs(entrada.getPrecio() - candidata.getPrecio());
            double rango = Math.max(entrada.getPrecio(), 1.0);
            if (diff / rango <= 0.3) score += 0.2;
        }
        return score;
    }

    public interface EstrategiaRecomendacion {
        List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite);
    }

    public static class RecomendacionPorContenido implements EstrategiaRecomendacion {
        @Override
        public List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite) {
            return candidatos.stream()
                    .sorted(Comparator.comparingDouble((Producto p) -> puntaje(entrada, p)).reversed())
                    .limit(limite)
                    .map(p -> toDto(entrada, p))
                    .collect(Collectors.toList());
        }
    }

    public static class RecomendacionComplementaria implements EstrategiaRecomendacion {
        @Override
        public List<RecomendacionDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite) {
            return candidatos.stream()
                    .filter(p -> !mismaCategoria(entrada.getCategoria(), p.getCategoria()))
                    .sorted(Comparator.comparingDouble((Producto p) -> puntaje(entrada, p)).reversed())
                    .limit(limite)
                    .map(p -> toDto(entrada, p))
                    .collect(Collectors.toList());
        }

        private boolean mismaCategoria(String c1, String c2) {
            return c1 != null && c1.equalsIgnoreCase(c2);
        }
    }

    public static class RecomendacionFactory {
        public static EstrategiaRecomendacion crear(String tipo) {
            if ("complementaria".equalsIgnoreCase(tipo)) {
                return new RecomendacionComplementaria();
            }
            return new RecomendacionPorContenido();
        }
    }

    private static RecomendacionDTO toDto(Producto entrada, Producto p) {
        return new RecomendacionDTO(p.getId(), p.getNombre(), p.getCategoria(),
                p.getPrecio(), p.getImagenUrl(), puntaje(entrada, p));
    }
}
