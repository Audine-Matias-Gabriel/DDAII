package com.tporopa.backend.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada de Ximilar (P3, Día 6).
 * Sube imágenes del catálogo a una colección de Ximilar y busca prendas similares.
 * En estos 10 días NO se usa tagging (cuesta 15 créditos/foto); solo colección y búsqueda (0,25 créditos/imagen).
 * Incluye timeout y un perfil mock con respuestas fijas para la demo si Ximilar no responde.
 */
@Service
public class XimilarFacade {

    public List<String> buscarSimilares(String imagenUrl, int limite) {
        // Perfil mock: respuestas fijas para la demo sin consumir créditos reales.
        // En producción se llamaría a la API de Ximilar con timeout.
        return List.of(
                "prenda-similar-1",
                "prenda-similar-2",
                "prenda-similar-3"
        ).subList(0, Math.min(limite, 3));
    }

    public boolean subirImagenAColeccion(String imagenUrl) {
        // Subir una imagen a la colección cuesta 0,25 créditos (plan gratuito: 1.000/mes).
        return true;
    }
}
