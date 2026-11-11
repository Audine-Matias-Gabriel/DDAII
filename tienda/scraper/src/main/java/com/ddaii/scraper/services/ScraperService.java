package com.ddaii.scraper.services;

import com.ddaii.scraper.events.ProductoScrapeado;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orquesta el scraping y publica cada prenda como ProductoScrapeado en RabbitMQ (P3 Día 4).
 */
@Service
public class ScraperService {

    private final List<ScraperTienda> tiendas;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.scraper.exchange:tienda.exchange}")
    private String exchange;

    @Value("${app.scraper.queue:producto.scrapeado.queue}")
    private String queue;

    public ScraperService(List<ScraperTienda> tiendas, RabbitTemplate rabbitTemplate) {
        this.tiendas = tiendas;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void ejecutarScrapeado() {
        for (ScraperTienda tienda : tiendas) {
            List<ProductoScrapeado> catalogo = tienda.extraerCatalogo();
            for (ProductoScrapeado producto : catalogo) {
                try {
                    rabbitTemplate.convertAndSend(exchange, "producto.scrapeado", producto);
                    System.out.println("[scraper] Publicado: " + tienda.nombreTienda() + " - " + producto.getIdExterno());
                } catch (RuntimeException e) {
                    System.err.println("[scraper] Error publicando " + producto.getIdExterno() + ": " + e.getMessage());
                }
            }
        }
    }

    public Map<String, Object> resumen() {
        return Map.of(
                "tiendas", tiendas.size(),
                "nombres", tiendas.stream().map(ScraperTienda::nombreTienda).toList()
        );
    }
}
