package com.ddaii.scraper.services;

import com.ddaii.scraper.events.ProductoScrapeado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Tienda 2 como otra implementación de ScraperTienda (docs/PLAN-INTEGRACION.md 5).
 * Estrategia separada para una segunda tienda argentina con catálogo público.
 */
@Component("tienda2")
public class Tienda2Scraper implements ScraperTienda {

    public static final String NOMBRE = "tienda2";
    public static final Long TIENDA_ID = 2L;

    @Value("classpath:snapshots/tienda2.json")
    private Resource snapshotResource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String nombreTienda() {
        return NOMBRE;
    }

    @Override
    public List<ProductoScrapeado> extraerCatalogo() {
        return snapshot();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductoScrapeado> snapshot() {
        Resource resource = snapshotResource != null
                ? snapshotResource
                : new ClassPathResource("snapshots/tienda2.json");
        try (InputStream is = resource.getInputStream()) {
            List<Map<String, Object>> datos = objectMapper.readValue(is, List.class);
            return datos.stream().map(m -> new ProductoScrapeado(
                    TIENDA_ID,
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
            throw new RuntimeException("No se pudo leer el snapshot de tienda 2", e);
        }
    }
}
