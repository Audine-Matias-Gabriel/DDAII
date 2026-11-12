package com.ddaii.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * Los JSON de ejemplo de docs/eventos.md tienen que poder leerse como estos
 * records y volver a escribirse igual. Si cambia un campo, cambian los dos.
 */
class EventosContratoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void productoScrapeado_leeYEscribeElJsonDelContrato() {
        String json = """
                {"tienda":"tienda1","idExterno":"123","nombre":"Remera X","precio":25000,
                 "categoria":"REMERA","genero":"HOMBRE","marca":"X",
                 "urlOriginal":"https://tienda1.com/remera-x",
                 "imagenUrl":"https://tienda1.com/remera-x.jpg",
                 "disponible":true,"tallesDisponibles":["S","M"]}
                """;

        ProductoScrapeadoEvent evento =
                objectMapper.readValue(json, ProductoScrapeadoEvent.class);

        assertThat(evento.tienda()).isEqualTo("tienda1");
        assertThat(evento.idExterno()).isEqualTo("123");
        assertThat(evento.precio()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(evento.categoria()).isEqualTo("REMERA");
        assertThat(evento.genero()).isEqualTo("HOMBRE");
        assertThat(evento.disponible()).isTrue();
        assertThat(evento.tallesDisponibles()).containsExactly("S", "M");

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(evento)))
                .isEqualTo(objectMapper.readTree(json));
    }

    @Test
    void productoScrapeado_ignoraCamposDesconocidos() {
        String json = """
                {"tienda":"tienda1","idExterno":"1","nombre":"X","precio":1,
                 "categoria":"REMERA","genero":"UNISEX","urlOriginal":"https://x",
                 "disponible":false,"tallesDisponibles":[],"campoNuevo":"valor"}
                """;

        ProductoScrapeadoEvent evento =
                objectMapper.readValue(json, ProductoScrapeadoEvent.class);

        assertThat(evento.marca()).isNull();
        assertThat(evento.imagenUrl()).isNull();
        assertThat(evento.tallesDisponibles()).isEmpty();
    }

    @Test
    void productoScrapeado_sinTalles_quedaConListaVacia() {
        ProductoScrapeadoEvent evento = new ProductoScrapeadoEvent(
                "t", "1", "X", BigDecimal.ONE, "REMERA", "UNISEX",
                null, "https://x", null, false, null);

        assertThat(evento.tallesDisponibles()).isEmpty();
    }

    @Test
    void productoScrapeado_copiaLosTallesParaNoCompartirLaLista() {
        List<String> talles = new ArrayList<>(List.of("S"));
        ProductoScrapeadoEvent evento = new ProductoScrapeadoEvent(
                "t", "1", "X", BigDecimal.ONE, "REMERA", "UNISEX",
                null, "https://x", null, true, talles);

        talles.add("XL");

        assertThat(evento.tallesDisponibles()).containsExactly("S");
    }

    @Test
    void suscripcionActivada_leeYEscribeElJsonDelContrato() {
        String json = """
                {"suscripcionId":1,"usuarioId":7,"pagoId":"123456",
                 "fecha":"2026-11-12T10:00:00"}
                """;

        SuscripcionActivadaEvent evento =
                objectMapper.readValue(json, SuscripcionActivadaEvent.class);

        assertThat(evento.suscripcionId()).isEqualTo(1L);
        assertThat(evento.usuarioId()).isEqualTo(7L);
        assertThat(evento.pagoId()).isEqualTo("123456");
        assertThat(evento.fecha()).isEqualTo(LocalDateTime.of(2026, 11, 12, 10, 0));

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(evento)))
                .isEqualTo(objectMapper.readTree(json));
    }

    @Test
    void recomendacionSolicitada_leeYEscribeElJsonDelContrato() {
        String json = """
                {"solicitudId":"3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c",
                 "productoId":10,"limite":5}
                """;

        RecomendacionSolicitadaEvent evento =
                objectMapper.readValue(json, RecomendacionSolicitadaEvent.class);

        assertThat(evento.solicitudId()).isEqualTo("3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c");
        assertThat(evento.productoId()).isEqualTo(10L);
        assertThat(evento.limite()).isEqualTo(5);

        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(evento)))
                .isEqualTo(objectMapper.readTree(json));
    }
}
