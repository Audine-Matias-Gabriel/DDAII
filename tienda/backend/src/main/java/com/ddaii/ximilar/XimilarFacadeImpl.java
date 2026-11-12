package com.ddaii.ximilar;

import com.ddaii.domain.Producto;
import com.ddaii.ximilar.dto.XimilarSearchResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Profile("!mock")
public class XimilarFacadeImpl implements XimilarFacade {

    private final RestClient restClient;

    public XimilarFacadeImpl(RestClient ximilarRestClient, XimilarProperties props) {
        if (props.getApiToken() == null || props.getApiToken().isBlank()) {
            throw new IllegalStateException(
                    "XIMILAR_API_TOKEN no configurado. Definilo en .env o usá el perfil 'mock'.");
        }
        if (props.getCollectionId() == null || props.getCollectionId().isBlank()) {
            throw new IllegalStateException(
                    "XIMILAR_COLLECTION_ID no configurado. Definilo en .env o usá el perfil 'mock'.");
        }
        this.restClient = ximilarRestClient;
    }

    @Override
    public void indexarProducto(Producto producto) {
        if (producto == null || producto.getImagenUrl() == null || producto.getImagenUrl().isBlank()) {
            return;
        }

        Map<String, Object> record = Map.of(
                "_id", String.valueOf(producto.getId()),
                "_url", producto.getImagenUrl()
        );
        Map<String, Object> body = Map.of("records", List.of(record));

        restClient.post()
                .uri("/similarity/fashion-new/v2/insert")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<String> buscarSimilares(Producto productoOriginal, int limite) {
        if (productoOriginal == null
                || productoOriginal.getImagenUrl() == null
                || productoOriginal.getImagenUrl().isBlank()) {
            return List.of();
        }

        Map<String, Object> body = Map.of(
                "query_record", Map.of("_url", productoOriginal.getImagenUrl()),
                "fields_to_return", List.of("_id"),
                "k", limite
        );

        XimilarSearchResponse response = restClient.post()
                .uri("/similarity/fashion-new/v2/visual_search")
                .body(body)
                .retrieve()
                .body(XimilarSearchResponse.class);

        if (response == null || response.answerRecords() == null) {
            return List.of();
        }
        return response.answerRecords().stream()
                .map(XimilarSearchResponse.AnswerRecord::id)
                .filter(Objects::nonNull)
                .toList();
    }
}