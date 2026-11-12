package com.ddaii.scraper.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ddaii.scraper.events.ProductoScrapeado;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del convertidor de tienda 1 usando los JSON guardados (sin conexión a internet, P3 Día 7-8).
 */
class TestConvertidorTienda1 {

    @Test
    void snapshotTienda1TieneAlMenos30Prendas() throws Exception {
        ClassPathResource resource = new ClassPathResource("snapshots/tienda1.json");
        assertTrue(resource.exists(), "El snapshot de tienda 1 debe existir");

        try (InputStream is = resource.getInputStream()) {
            ObjectMapper om = new ObjectMapper();
            List<Map<String, Object>> datos = om.readValue(is, List.class);

            assertTrue(datos.size() >= 30, "Debe haber al menos 30 prendas en el snapshot");

            for (Map<String, Object> p : datos) {
                assertNotNull(p.get("idExterno"), "Cada prenda debe tener idExterno");
                assertNotNull(p.get("nombre"), "Cada prenda debe tener nombre");
                assertNotNull(p.get("precio"), "Cada prenda debe tener precio");
                assertNotNull(p.get("imagenUrl"), "Cada prenda debe tener imagenUrl");
                assertNotNull(p.get("urlOriginal"), "Cada prenda debe tener URL oficial de la tienda");
                assertNotNull(p.get("tallesDisponibles"), "Cada prenda debe tener talles disponibles");
            }
        }
    }

    @Test
    void tienda2ExtraeProductosDeOtraTienda() {
        Tienda2Scraper scraper = new Tienda2Scraper();
        List<ProductoScrapeado> productos = scraper.snapshot();
        assertTrue(productos.size() >= 10, "El scraper de tienda 2 debe tener snapshot");
        assertEquals("tienda2", productos.get(0).getTienda());
        assertEquals(2L, productos.get(0).getTiendaId());
    }

    @Test
    void tienda1ExtraeProductosEnFormatoAcordado() {
        Tienda1Scraper scraper = new Tienda1Scraper();
        List<ProductoScrapeado> productos = scraper.snapshot();
        assertTrue(productos.size() >= 30, "El scraper debe extraer al menos 30 productos");
        ProductoScrapeado primero = productos.get(0);
        assertEquals("tienda1", primero.getTienda());
        assertNotNull(primero.getIdExterno());
        assertNotNull(primero.getImagenUrl());
        assertNotNull(primero.getTallesDisponibles());
    }
}
