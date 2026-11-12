package com.ddaii.integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP real para la API de Ximilar (Product Similarity).
 * Endpoints usados:
 *   POST /similarity/products-new/v2/visualKNN  -> buscar similares por URL de imagen
 *   POST /similarity/products-new/v2/insert     -> subir imagen a la colección
 *
 * Credenciales leídas de application.yml (variables de entorno):
 *   ximilar.api-key    -> XIMILAR_API_KEY  (o XIMILAR_API_TOKEN)
 *   ximilar.collection-id -> XIMILAR_COLLECTION_ID
 *   ximilar.base-url   -> XIMILAR_BASE_URL (por defecto https://api.ximilar.com)
 */
@Component
public class XimilarClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ximilar.api-key:}")
    private String apiKey;

    @Value("${ximilar.collection-id:}")
    private String collectionId;

    @Value("${ximilar.base-url:https://api.ximilar.com}")
    private String baseUrl;

    /**
     * Busca prendas similares a partir de una URL de imagen.
     * Devuelve una lista de IDs de producto (Long) encontrados por Ximilar.
     * En caso de error (sin credenciales, red caída, etc.) devuelve lista vacía
     * para que el servicio use el fallback local.
     */
    public List<Long> buscarSimilares(String imagenUrl, int limite) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return List.of();
        }
        if (apiKey == null || apiKey.isBlank() || collectionId == null || collectionId.isBlank()) {
            return List.of();
        }

        String url = baseUrl + "/similarity/products-new/v2/visualKNN";

        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, String> queryRecord = new LinkedHashMap<>();
        queryRecord.put("_url", imagenUrl);
        body.put("query_record", queryRecord);
        body.put("k", Math.max(1, Math.min(limite, 10)));
        body.put("fields_to_return", List.of("_id", "_url", "product_id"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + apiKey);
        headers.set("collection-id", collectionId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode answerRecords = root.path("answer_records");
            List<Long> ids = new ArrayList<>();
            if (answerRecords.isArray()) {
                for (JsonNode record : answerRecords) {
                    String productId = record.path("product_id").asText();
                    if (productId != null && !productId.isBlank()) {
                        try {
                            ids.add(Long.parseLong(productId));
                        } catch (NumberFormatException ignored) {
                            // ID no numérico, se ignora
                        }
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            // Sin red o error de Ximilar: fallback vacío
            return List.of();
        }
    }

    /**
     * Sube una imagen a la colección de Ximilar (costo: 0,25 créditos por imagen).
     * Devuelve true si se envió correctamente.
     */
    public boolean subirImagenAColeccion(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return false;
        }
        if (apiKey == null || apiKey.isBlank() || collectionId == null || collectionId.isBlank()) {
            return false;
        }

        String url = baseUrl + "/similarity/products-new/v2/insert";

        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> record = new LinkedHashMap<>();
        record.put("_url", imagenUrl);
        records.add(record);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("records", records);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + apiKey);
        headers.set("collection-id", collectionId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
