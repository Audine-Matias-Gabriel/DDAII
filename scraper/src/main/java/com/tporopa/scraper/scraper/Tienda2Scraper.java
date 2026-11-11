package com.tporopa.scraper.scraper;

import com.tporopa.scraper.model.ProductoEvento;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tienda 2 como otra implementación de ScraperTienda (P3 Día 5).
 * Estrategia separada para una segunda tienda argentina con catálogo público.
 */
@Component("tienda2")
public class Tienda2Scraper implements ScraperTienda {

    public static final String NOMBRE = "tienda2";

    @Override
    public String nombreTienda() {
        return NOMBRE;
    }

    @Override
    public List<ProductoEvento> extraerCatalogo() {
        // Segunda tienda: mismo patrón, fuente distinta (ej. VTEX u otra Shopify).
        return List.of();
    }

    @Override
    public List<ProductoEvento> snapshot() {
        return List.of();
    }
}
