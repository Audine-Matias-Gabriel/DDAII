package com.tporopa.scraper.scraper;

import com.tporopa.scraper.model.ProductoEvento;
import java.util.List;

/**
 * Interfaz de scraper por tienda (patrón Strategy, P3 Día 3).
 * Cada tienda implementa su propia conversión al formato acordado (docs/modelo.md).
 */
public interface ScraperTienda {

    String nombreTienda();

    /** Lee el catálogo de la tienda y devuelve productos en formato acordado. */
    List<ProductoEvento> extraerCatalogo();

    /** Devuelve el snapshot guardado (para la demo sin internet). */
    List<ProductoEvento> snapshot();
}
