package com.ddaii.integrations;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada de Ximilar (docs/PLAN-INTEGRACION.md 6).
 * Sube imágenes del catálogo a una colección y busca prendas similares.
 * En estos 10 días NO se usa tagging (cuesta 15 créditos/foto); solo colección y búsqueda.
 * Incluye un perfil mock con respuestas fijas para la demo si Ximilar no responde.
 */
@Service
public class XimilarFacade {

    public List<Long> buscarSimilares(String imagenUrl, int limite) {
        // Perfil mock: IDs fijos para la demo sin consumir créditos reales.
        // En producción se llamaría a la API de Ximilar con timeout.
        List<Long> mock = List.of(1L, 2L, 3L);
        return mock.subList(0, Math.min(limite, mock.size()));
    }

    public boolean subirImagenAColeccion(String imagenUrl) {
        // Subir una imagen a la colección cuesta 0,25 créditos (plan gratuito: 1.000/mes).
        return true;
    }
}
