package com.tporopa.scraper.scraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tporopa.scraper.model.ProductoEvento;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.scraper.exchange:webropa.events}")
    private String exchange;

    @Value("${app.scraper.queue:cola.producto.scrapeado}")
    private String queue;

    public ScraperService(List<ScraperTienda> tiendas, RabbitTemplate rabbitTemplate) {
        this.tiendas = tiendas;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void ejecutarScrapeado() {
        for (ScraperTienda tienda : tiendas) {
            List<ProductoEvento> catalogo = tienda.extraerCatalogo();
            for (ProductoEvento producto : catalogo) {
                try {
                    String json = objectMapper.writeValueAsString(producto);
                    rabbitTemplate.convertAndSend(exchange, "evento.producto.scrapeado", json);
                    System.out.println("[scraper] Publicado: " + tienda.nombreTienda() + " - " + producto.getIdExterno());
                } catch (JsonProcessingException e) {
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
