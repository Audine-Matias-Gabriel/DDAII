package com.ddaii.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Un producto leído de una tienda por el scraper. Publica el scraper (P3) y
 * consume el backend (P2). Contrato en docs/eventos.md.
 *
 * {@code categoria} y {@code genero} van como texto y no como enum: el valor
 * lo decide el scraper y el backend lo valida al consumir. Así un valor nuevo
 * no rompe la lectura del mensaje.
 */
public record ProductoScrapeadoEvent(
        String tienda,
        String idExterno,
        String nombre,
        BigDecimal precio,
        String categoria,
        String genero,
        String marca,
        String urlOriginal,
        String imagenUrl,
        boolean disponible,
        List<String> tallesDisponibles
) {
    public ProductoScrapeadoEvent {
        tallesDisponibles = tallesDisponibles == null
                ? List.of()
                : List.copyOf(tallesDisponibles);
    }
}
