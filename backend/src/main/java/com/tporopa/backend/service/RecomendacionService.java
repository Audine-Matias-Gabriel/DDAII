package com.tporopa.backend.service;

import com.tporopa.backend.dto.ProductoDTO;
import com.tporopa.backend.event.RecomendacionSolicitada;
import com.tporopa.backend.model.Producto;
import com.tporopa.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente de IA de recomendaciones por contenido (P4).
 * Día 2-6: regla del recomendador, EstrategiaRecomendacion (Strategy),
 * RecomendacionPorContenido, RecomendacionComplementaria, RecomendacionFactory.
 * Día 4: GET /api/recomendaciones (REST).
 * Día 5: POST /api/recomendaciones/solicitudes (cola, responde 202).
 */
@Service
public class RecomendacionService {

    private final ProductoRepository productoRepository;
    private final EventPublisher eventPublisher;

    public RecomendacionService(ProductoRepository productoRepository, EventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<ProductoDTO> recomendar(Long productoId, Integer limite) {
        Producto entrada = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        List<Producto> candidatos = productoRepository.findByDisponibleTrue().stream()
                .filter(p -> !p.getId().equals(entrada.getId()))
                .collect(Collectors.toList());

        EstrategiaRecomendacion estrategia = RecomendacionFactory.crear("contenido");
        return estrategia.recomendar(entrada, candidatos, limite != null ? limite : 5);
    }

    @Transactional
    public void solicitarPorCola(Long productoId, Integer limite) {
        // Publica el evento y responde 202 (procesamiento asíncrono).
        eventPublisher.publicarRecomendacionSolicitada(productoId, limite != null ? limite : 5);
        // El consumidor propio procesa y guarda el resultado (simulado síncrono para la demo).
        recomendar(productoId, limite);
    }

    public static List<ProductoDTO> toDtoList(List<Producto> productos) {
        return productos.stream().map(p -> new ProductoDTO(
                p.getId(), p.getIdExterno(), p.getTienda(), p.getNombre(),
                p.getDescripcion(), p.getPrecio(), p.getCategoria(), p.getGenero(),
                p.getMarca(), p.getUrlOriginal(), p.getImagenUrl(),
                p.getDisponible(), p.getTallesDisponibles()
        )).collect(Collectors.toList());
    }

    /** Puntaje simple de una prenda candidata frente a la prenda de entrada. */
    public static double puntaje(Producto entrada, Producto candidata) {
        double score = 0.0;
        if (entrada.getCategoria() != null && entrada.getCategoria().equalsIgnoreCase(candidata.getCategoria())) score += 0.4;
        if (entrada.getGenero() != null && entrada.getGenero().equalsIgnoreCase(candidata.getGenero())) score += 0.2;
        if (entrada.getMarca() != null && entrada.getMarca().equalsIgnoreCase(candidata.getMarca())) score += 0.2;
        if (entrada.getPrecio() != null && candidata.getPrecio() != null) {
            double diff = Math.abs(entrada.getPrecio() - candidata.getPrecio());
            double rango = Math.max(entrada.getPrecio(), 1.0);
            if (diff / rango <= 0.3) score += 0.2; // precio parecido (±30%)
        }
        return score;
    }

    public interface EstrategiaRecomendacion {
        List<ProductoDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite);
    }

    public static class RecomendacionPorContenido implements EstrategiaRecomendacion {
        @Override
        public List<ProductoDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite) {
            return candidatos.stream()
                    .sorted((a, b) -> Double.compare(puntaje(entrada, b), puntaje(entrada, a)))
                    .limit(limite)
                    .map(p -> new ProductoDTO(
                            p.getId(), p.getIdExterno(), p.getTienda(), p.getNombre(),
                            p.getDescripcion(), p.getPrecio(), p.getCategoria(), p.getGenero(),
                            p.getMarca(), p.getUrlOriginal(), p.getImagenUrl(),
                            p.getDisponible(), p.getTallesDisponibles()))
                    .collect(Collectors.toList());
        }
    }

    public static class RecomendacionComplementaria implements EstrategiaRecomendacion {
        @Override
        public List<ProductoDTO> recomendar(Producto entrada, List<Producto> candidatos, int limite) {
            // Arma un conjunto: parte de arriba, parte de abajo y calzado.
            return candidatos.stream()
                    .filter(p -> !mismaCategoria(entrada.getCategoria(), p.getCategoria()))
                    .sorted((a, b) -> Double.compare(puntaje(entrada, b), puntaje(entrada, a)))
                    .limit(limite)
                    .map(p -> new ProductoDTO(
                            p.getId(), p.getIdExterno(), p.getTienda(), p.getNombre(),
                            p.getDescripcion(), p.getPrecio(), p.getCategoria(), p.getGenero(),
                            p.getMarca(), p.getUrlOriginal(), p.getImagenUrl(),
                            p.getDisponible(), p.getTallesDisponibles()))
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
}
