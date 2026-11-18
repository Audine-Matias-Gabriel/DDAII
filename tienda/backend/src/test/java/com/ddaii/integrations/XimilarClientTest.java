package com.ddaii.integrations;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Contrato con la API de Ximilar Product Similarity, sin internet (servidor HTTP falso). */
class XimilarClientTest {

    private static final String BASE = "https://api.ximilar.test";

    private XimilarClient cliente(String key) {
        return new XimilarClient(new RestTemplateBuilder(), key, "coleccion-1", BASE);
    }

    @Test
    void insertaHasta10RegistrosConIdProductoYTienda() {
        XimilarClient cliente = cliente("token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(cliente.restTemplate()).build();
        server.expect(requestTo(BASE + "/similarity/products-new/v2/insert"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Token token"))
                .andExpect(header("collection-id", "coleccion-1"))
                .andExpect(jsonPath("$.records[0]._id").value("producto-7"))
                .andExpect(jsonPath("$.records[0]._url").value("https://img.test/7.jpg"))
                .andExpect(jsonPath("$.records[0].product_id").value("7"))
                .andExpect(jsonPath("$.records[0].tienda_id").value(2))
                .andRespond(withSuccess("""
                        {"status":{"code":211,"text":"some records inserted"},
                         "skipped_records":[{"_id":"producto-9","reason":"record(s) duplicate"},
                                            {"_id":"producto-8","reason":"cannot download image"}]}
                        """, MediaType.APPLICATION_JSON));

        var resultado = cliente.insertar(List.of(
                new XimilarClient.Registro(7L, "https://img.test/7.jpg", 2L),
                new XimilarClient.Registro(8L, "https://img.test/8.jpg", 2L),
                new XimilarClient.Registro(9L, "https://img.test/9.jpg", 2L)));

        // El duplicado ya estaba en la colección: cuenta como subido. La imagen rota falla.
        assertThat(resultado.subidos()).containsExactlyInAnyOrder(7L, 9L);
        assertThat(resultado.fallidos()).containsExactly(8L);
        server.verify();
    }

    @Test
    void buscaPorIdFiltrandoOtraTiendaYDevuelveIdsEnOrden() {
        XimilarClient cliente = cliente("token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(cliente.restTemplate()).build();
        server.expect(requestTo(BASE + "/similarity/products-new/v2/visualKNN"))
                .andExpect(jsonPath("$.query_record._id").value("producto-1"))
                .andExpect(jsonPath("$.filter.tienda_id.$ne").value(1))
                .andRespond(withSuccess("""
                        {"status":{"code":200,"text":"OK"},
                         "answer_records":[{"_id":"producto-300","product_id":"300"},
                                           {"_id":"producto-1","product_id":"1"},
                                           {"_id":"producto-301"}],
                         "answer_distances":[0.1,0.2,0.3]}
                        """, MediaType.APPLICATION_JSON));

        List<Long> ids = cliente.buscarSimilares(1L, "https://img.test/1.jpg", true, 1L, 4);

        assertThat(ids).containsExactly(300L, 301L); // sin el propio producto
        server.verify();
    }

    @Test
    void siNoEstaEnLaColeccionBuscaPorUrl() {
        XimilarClient cliente = cliente("token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(cliente.restTemplate()).build();
        server.expect(requestTo(BASE + "/similarity/products-new/v2/visualKNN"))
                .andExpect(jsonPath("$.query_record._url").value("https://img.test/1.jpg"))
                .andRespond(withSuccess("{\"answer_records\":[]}", MediaType.APPLICATION_JSON));

        assertThat(cliente.buscarSimilares(1L, "https://img.test/1.jpg", false, 1L, 4)).isEmpty();
        server.verify();
    }

    @Test
    void siXimilarFallaDevuelveVacioYNoRompe() {
        XimilarClient cliente = cliente("token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(cliente.restTemplate()).build();
        server.expect(requestTo(BASE + "/similarity/products-new/v2/visualKNN")).andRespond(withServerError());
        server.expect(requestTo(BASE + "/similarity/products-new/v2/insert")).andRespond(withServerError());

        assertThat(cliente.buscarSimilares(1L, "u", true, 1L, 4)).isEmpty();
        var resultado = cliente.insertar(List.of(new XimilarClient.Registro(5L, "u", 1L)));
        assertThat(resultado.subidos()).isEmpty();
        assertThat(resultado.fallidos()).containsExactly(5L);
    }

    @Test
    void sinCredencialesNoLlamaALaApi() {
        XimilarClient cliente = cliente("");
        MockRestServiceServer server = MockRestServiceServer.bindTo(cliente.restTemplate()).build();

        assertThat(cliente.configurado()).isFalse();
        assertThat(cliente.buscarSimilares(1L, "u", true, 1L, 4)).isEmpty();
        assertThat(cliente.insertar(List.of(new XimilarClient.Registro(5L, "u", 1L))).subidos()).isEmpty();
        server.verify(); // ninguna llamada
    }
}
