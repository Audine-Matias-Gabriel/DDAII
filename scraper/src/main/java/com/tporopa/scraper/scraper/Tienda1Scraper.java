package com.tporopa.scraper.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tporopa.scraper.model.ProductoEvento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Implementación de ScraperTienda para la tienda 1 (P3 Día 2-3).
 * Lee el catálogo de la tienda y lo convierte al formato acordado con P2.
 * Guarda el resultado en scraper/snapshots/tienda1.json (≥30 prendas).
 */
@Component("tienda1")
public class Tienda1Scraper implements ScraperTienda {

    public static final String NOMBRE = "tienda1";

    @Value("classpath:snapshots/tienda1.json")
    private Resource snapshotResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String nombreTienda() {
        return NOMBRE;
    }

    @Override
    public List<ProductoEvento> extraerCatalogo() {
        // En producción se leería la URL del catálogo público de la tienda (ej. /products.json de Shopify)
        // y se convertiría cada prenda al formato acordado. Aquí usamos el snapshot para la demo.
        return snapshot();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductoEvento> snapshot() {
        try (InputStream is = snapshotResource.getInputStream()) {
            List<Map<String, Object>> datos = objectMapper.readValue(is, List.class);
            return datos.stream().map(m -> new ProductoEvento(
                    (String) m.get("tienda"),
                    (String) m.get("idExterno"),
                    (String) m.get("nombre"),
                    (String) m.get("descripcion"),
                    ((Number) m.get("precio")).doubleValue(),
                    (String) m.get("categoria"),
                    (String) m.get("genero"),
                    (String) m.get("marca"),
                    (String) m.get("urlOriginal"),
                    (String) m.get("imagenUrl"),
                    (Boolean) m.get("disponible"),
                    (List<String>) m.get("tallesDisponibles")
            )).toList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el snapshot de tienda 1", e);
        }
    }
}
