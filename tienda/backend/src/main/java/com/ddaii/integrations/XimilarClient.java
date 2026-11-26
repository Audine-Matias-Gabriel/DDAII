package com.ddaii.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cliente HTTP de Ximilar Product Similarity (docs: https://docs.ximilar.com/visual-search/overview).
 *
 *   POST {base}/similarity/products-new/v2/insert     máx. 10 registros por llamada; un _id repetido se saltea
 *   POST {base}/similarity/products-new/v2/delete     para cambiar la imagen hay que borrar y volver a insertar
 *   POST {base}/similarity/products-new/v2/visualKNN  búsqueda por _id (consulta interna) o por _url (externa)
 *
 * Cada registro es un producto: {@code _id = "producto-<id>"}, {@code _url}, {@code product_id} y
 * {@code tienda_id} (este último para filtrar "otras tiendas" en la búsqueda).
 *
 * Créditos (ximilar.com/pricing, Product Similarity): insertar 0,25; buscar por _id 0,25; buscar por URL 1.
 * Plan gratuito: 1.000 créditos por mes y colecciones de hasta 1.000 registros.
 *
 * Credenciales: XIMILAR_API_KEY y XIMILAR_COLLECTION_ID. Sin ellas no se llama a la API.
 */
@Component
public class XimilarClient {

    private static final Logger log = LoggerFactory.getLogger(XimilarClient.class);
    public static final int MAX_REGISTROS_POR_INSERT = 10;
    private static final String SERVICIO = "/similarity/products-new/v2";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String collectionId;
    private final String baseUrl;

    public XimilarClient(RestTemplateBuilder builder,
                         @Value("${ximilar.api-key:}") String apiKey,
                         @Value("${ximilar.collection-id:}") String collectionId,
                         @Value("${ximilar.base-url:https://api.ximilar.com}") String baseUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.apiKey = apiKey;
        this.collectionId = collectionId;
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
    }

    /** Un producto para la colección. */
    public record Registro(Long productoId, String imagenUrl, Long tiendaId) {}

    /** Resultado de un insert: ids subidos (o que ya estaban) y fallidos. */
    public record ResultadoInsert(Set<Long> subidos, Set<Long> fallidos) {}

    /** Producto devuelto por una búsqueda visual y su distancia (menor = más parecido). */
    public record Coincidencia(Long productoId, Double distancia) {}

    public boolean configurado() {
        return apiKey != null && !apiKey.isBlank() && collectionId != null && !collectionId.isBlank();
    }

    RestTemplate restTemplate() {
        return restTemplate;
    }

    static String idRegistro(Long productoId) {
        return "producto-" + productoId;
    }

    static Long productoDeRegistro(JsonNode record) {
        String productId = record.path("product_id").asText("");
        if (productId.isBlank()) {
            String id = record.path("_id").asText("");
            productId = id.startsWith("producto-") ? id.substring("producto-".length()) : "";
        }
        try {
            return productId.isBlank() ? null : Long.parseLong(productId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sube hasta 10 productos a la colección. Un _id que ya existe cuenta como subido
     * (Ximilar lo saltea como duplicado, no gasta un insert nuevo).
     */
    public ResultadoInsert insertar(List<Registro> registros) {
        Set<Long> subidos = new HashSet<>();
        Set<Long> fallidos = new HashSet<>();
        if (registros.isEmpty()) return new ResultadoInsert(subidos, fallidos);
        if (registros.size() > MAX_REGISTROS_POR_INSERT) {
            throw new IllegalArgumentException("Ximilar acepta hasta " + MAX_REGISTROS_POR_INSERT + " registros por insert");
        }
        if (!configurado()) {
            registros.forEach(r -> fallidos.add(r.productoId()));
            return new ResultadoInsert(subidos, fallidos);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Registro r : registros) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("_id", idRegistro(r.productoId()));
            record.put("_url", r.imagenUrl());
            record.put("product_id", String.valueOf(r.productoId()));
            record.put("tienda_id", r.tiendaId());
            records.add(record);
        }

        try {
            JsonNode respuesta = post("/insert", Map.of("records", records));
            registros.forEach(r -> subidos.add(r.productoId()));
            // Los salteados por otro motivo que no sea duplicado (ej. imagen que no se pudo bajar) fallan.
            for (JsonNode salteado : respuesta.path("skipped_records")) {
                Long id = productoDeRegistro(salteado);
                String motivo = salteado.toString().toLowerCase();
                if (id != null && !motivo.contains("duplicate")) {
                    subidos.remove(id);
                    fallidos.add(id);
                }
            }
        } catch (Exception e) {
            log.warn("Ximilar insert falló ({} registros): {}", registros.size(), e.getMessage());
            registros.forEach(r -> fallidos.add(r.productoId()));
            subidos.clear();
        }
        return new ResultadoInsert(subidos, fallidos);
    }

    /** Borra productos de la colección (antes de volver a subirlos con otra imagen). */
    public boolean eliminar(List<Long> productoIds) {
        if (productoIds.isEmpty() || !configurado()) return false;
        List<Map<String, String>> records = productoIds.stream()
                .map(id -> Map.of("_id", idRegistro(id)))
                .toList();
        try {
            post("/delete", Map.of("records", records));
            return true;
        } catch (Exception e) {
            log.warn("Ximilar delete falló: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Productos visualmente parecidos de OTRA tienda, del más al menos parecido.
     * Si el producto está en la colección busca por su _id (0,25 créditos); si no, por la URL de su imagen (1 crédito).
     * Lista vacía si no hay credenciales o si Ximilar falla: el servicio usa su respaldo.
     */
    public List<Long> buscarSimilares(Long productoId, String imagenUrl, boolean indexado, Long excluirTiendaId, int limite) {
        if (!configurado()) return List.of();
        if (!indexado && (imagenUrl == null || imagenUrl.isBlank())) return List.of();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query_record", indexado
                ? Map.of("_id", idRegistro(productoId))
                : Map.of("_url", imagenUrl));
        body.put("k", Math.max(1, Math.min(limite + 1, 30)));
        body.put("fields_to_return", List.of("_id", "product_id"));
        // $exists deja afuera registros viejos de la colección que no son productos del catálogo.
        Map<String, Object> condicionTienda = new LinkedHashMap<>();
        condicionTienda.put("$exists", true);
        if (excluirTiendaId != null) {
            condicionTienda.put("$ne", excluirTiendaId);
        }
        body.put("filter", Map.of("tienda_id", condicionTienda));

        try {
            JsonNode respuesta = post("/visualKNN", body);
            List<Long> ids = new ArrayList<>();
            for (JsonNode record : respuesta.path("answer_records")) {
                Long id = productoDeRegistro(record);
                if (id != null && !id.equals(productoId) && !ids.contains(id)) ids.add(id);
            }
            return ids;
        } catch (Exception e) {
            log.warn("Ximilar visualKNN falló para el producto {}: {}", productoId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Búsqueda visual con una imagen subida por el usuario (no hace falta publicarla en una URL).
     * Ximilar acepta el campo {@code _base64} con el prefijo data URI. Se pide el orden por
     * distancia y solo se devuelven registros del catálogo (tienen {@code tienda_id}).
     * A diferencia de {@link #buscarSimilares}, un fallo se propaga para no confundirlo con
     * "sin coincidencias" y poder responder 502.
     */
    public List<Coincidencia> buscarPorImagen(byte[] imagen, String contentType, int k) {
        if (!configurado()) {
            throw new IllegalStateException("Ximilar no está configurado");
        }
        if (imagen == null || imagen.length == 0) {
            throw new IllegalArgumentException("La imagen está vacía");
        }
        String mime = contentType == null || contentType.isBlank() ? "image/jpeg" : contentType;
        String base64 = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(imagen);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query_record", Map.of("_base64", base64));
        body.put("k", Math.max(1, Math.min(k, 30)));
        body.put("fields_to_return", List.of("_id", "product_id"));
        body.put("filter", Map.of("tienda_id", Map.of("$exists", true)));

        try {
            JsonNode respuesta = post("/visualKNN", body);
            JsonNode registros = respuesta.path("answer_records");
            JsonNode distancias = respuesta.path("answer_distances");
            List<Coincidencia> coincidencias = new ArrayList<>();
            Set<Long> vistos = new HashSet<>();
            for (int i = 0; i < registros.size(); i++) {
                Long id = productoDeRegistro(registros.get(i));
                if (id == null || !vistos.add(id)) continue;
                Double distancia = null;
                if (distancias.isArray() && i < distancias.size() && distancias.get(i).isNumber()) {
                    distancia = distancias.get(i).asDouble();
                }
                coincidencias.add(new Coincidencia(id, distancia));
            }
            return coincidencias;
        } catch (Exception e) {
            throw new IllegalStateException("Ximilar visualKNN falló: " + e.getMessage(), e);
        }
    }

    private JsonNode post(String metodo, Map<String, Object> body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + apiKey);
        headers.set("collection-id", collectionId);
        String respuesta = restTemplate.exchange(
                baseUrl + SERVICIO + metodo, HttpMethod.POST, new HttpEntity<>(body, headers), String.class
        ).getBody();
        JsonNode json = objectMapper.readTree(respuesta == null ? "{}" : respuesta);
        int codigo = json.path("status").path("code").asInt(200);
        if (codigo >= 300 && codigo != 411) { // 411 = todos duplicados (ya estaban)
            throw new IllegalStateException("Ximilar respondió " + json.path("status"));
        }
        return json;
    }
}
