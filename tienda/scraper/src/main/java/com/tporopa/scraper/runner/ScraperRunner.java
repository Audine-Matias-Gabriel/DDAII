package com.tporopa.scraper.runner;

import com.tporopa.scraper.scraper.ScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Ejecuta el scraping al arrancar (comando a mano para la demo, P3 Día 5).
 * En producción se programaría una ejecución automática 1 vez por día.
 */
@Component
public class ScraperRunner implements CommandLineRunner {

    private final ScraperService scraperService;

    public ScraperRunner(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @Override
    public void run(String... args) {
        System.out.println("[scraper] Iniciando scrapeo de catálogos...");
        scraperService.ejecutarScrapeado();
        System.out.println("[scraper] Scrapeo finalizado.");
    }
}
