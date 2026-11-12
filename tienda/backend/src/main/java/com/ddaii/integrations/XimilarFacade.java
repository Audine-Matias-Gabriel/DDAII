package com.ddaii.integrations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada de Ximilar (docs/PLAN-INTEGRACION.md 6).
 * Sube imágenes del catálogo a una colección y busca prendas similares.
 *
 * Por defecto usa la API real de Ximilar. Se puede forzar modo mock con la variable
 * de entorno XIMILAR_MOCK=true para la demo sin consumir créditos.
 *
 * Límites plan gratuito: 1.000 créditos/mes. Subir una imagen = 0,25 créditos.
 * No se usa tagging (cuesta 15 créditos/foto).
 */
@Service
public class XimilarFacade {

    private final XimilarClient ximilarClient;
    private final boolean mockMode;

    public XimilarFacade(XimilarClient ximilarClient,
                         @Value("${ximilar.mock:false}") boolean mockMode) {
        this.ximilarClient = ximilarClient;
        this.mockMode = mockMode;
    }

    /**
     * Busca IDs de productos similares en Ximilar.
     * En modo mock devuelve IDs fijos (1, 2, 3) para la demo.
     */
    public List<Long> buscarSimilares(String imagenUrl, int limite) {
        if (mockMode) {
            List<Long> mock = List.of(1L, 2L, 3L);
            return mock.subList(0, Math.min(limite, mock.size()));
        }
        return ximilarClient.buscarSimilares(imagenUrl, limite);
    }

    /**
     * Sube una imagen a la colección de Ximilar.
     * En modo mock devuelve true sin consumir créditos.
     */
    public boolean subirImagenAColeccion(String imagenUrl) {
        if (mockMode) {
            return true;
        }
        return ximilarClient.subirImagenAColeccion(imagenUrl);
    }
}
