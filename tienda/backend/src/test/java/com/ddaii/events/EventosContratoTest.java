package com.ddaii.events;

import com.ddaii.messaging.AmqpConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrato de los mensajes de RabbitMQ (docs/eventos.md), probado con las clases que de verdad se
 * publican y consumen y con el mismo conversor que usa la aplicación (AmqpConfig, tipo INFERRED:
 * el tipo sale del parámetro del @RabbitListener, no de un header __TypeId__).
 *
 * Si cambia un campo, hay que cambiar este test y docs/eventos.md en el mismo PR.
 */
class EventosContratoTest {

    private final Jackson2JsonMessageConverter conversor = new AmqpConfig().jackson2JsonMessageConverter();
    private final ObjectMapper json = new ObjectMapper();

    /** Como llega un mensaje publicado por otro servicio: solo JSON y content-type, sin __TypeId__. */
    private Object recibir(String cuerpo, Class<?> tipoDelListener) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setInferredArgumentType(tipoDelListener);
        return conversor.fromMessage(new Message(cuerpo.getBytes(StandardCharsets.UTF_8), props));
    }

    private JsonNode publicar(Object evento) throws Exception {
        Message m = conversor.toMessage(evento, new MessageProperties());
        assertThat(m.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        return json.readTree(m.getBody());
    }

    @Test
    void productoScrapeado_leeElJsonExactoQueMandaElScraperNode() {
        // Salida de tienda/scraper/src/events/ProductoScrapeadoEvent.js
        String delScraper = """
                {"tipo":"ProductoScrapeado","tiendaId":2,"tienda":"Grid","idExterno":"52974",
                 "nombre":"REMERA NBA CHICAGO BULLS HOMBRE","descripcion":"Remera de algodón",
                 "precio":24999,"categoria":"REMERA","genero":"Hombre","marca":"NBA",
                 "urlOriginal":"https://www.grid.com.ar/remera-nba-chicago-bulls/p",
                 "imagenUrl":"https://essential.vteximg.com.br/arquivos/ids/1/remera.jpg",
                 "disponible":true,"tallesDisponibles":["M","L"]}
                """;

        ProductoScrapeado e = (ProductoScrapeado) recibir(delScraper, ProductoScrapeado.class);

        assertThat(e.getTipo()).isEqualTo("ProductoScrapeado");
        assertThat(e.getTiendaId()).isEqualTo(2L);
        assertThat(e.getTienda()).isEqualTo("Grid");
        assertThat(e.getIdExterno()).isEqualTo("52974");
        assertThat(e.getDescripcion()).isEqualTo("Remera de algodón");
        assertThat(e.getPrecio()).isEqualTo(24999.0);
        assertThat(e.getCategoria()).isEqualTo("REMERA");
        assertThat(e.getGenero()).isEqualTo("Hombre");
        assertThat(e.getDisponible()).isTrue();
        assertThat(e.getTallesDisponibles()).containsExactly("M", "L");
    }

    @Test
    void productoScrapeado_ignoraCamposNuevosYAceptaOpcionalesFaltantes() {
        String minimo = """
                {"tienda":"Grid","idExterno":"1","nombre":"X","precio":1.5,"disponible":false,"campoNuevo":"x"}
                """;

        ProductoScrapeado e = (ProductoScrapeado) recibir(minimo, ProductoScrapeado.class);

        assertThat(e.getPrecio()).isEqualTo(1.5);
        assertThat(e.getMarca()).isNull();
        assertThat(e.getTallesDisponibles()).isNull(); // el upsert lo toma como lista vacía
    }

    @Test
    void suscripcionActivada_publicaLosCamposDelContratoYSeVuelveALeer() throws Exception {
        JsonNode enLaCola = publicar(new SuscripcionActivada(4L, 5L, "190338481640"));

        assertThat(enLaCola.get("tipo").asText()).isEqualTo("SuscripcionActivada");
        assertThat(enLaCola.get("usuarioId").asLong()).isEqualTo(4L);
        assertThat(enLaCola.get("suscripcionId").asLong()).isEqualTo(5L);
        assertThat(enLaCola.get("idPago").asText()).isEqualTo("190338481640");
        assertThat(enLaCola.get("estado").asText()).isEqualTo("ACTIVA");
        assertThat(enLaCola.has("fecha")).isTrue();

        SuscripcionActivada leida = (SuscripcionActivada) recibir(enLaCola.toString(), SuscripcionActivada.class);
        assertThat(leida.getUsuarioId()).isEqualTo(4L);
        assertThat(leida.getIdPago()).isEqualTo("190338481640");
        assertThat(leida.getFecha()).isNotNull();
    }

    @Test
    void recomendacionSolicitada_publicaLosCamposDelContratoYSeVuelveALeer() throws Exception {
        JsonNode enLaCola = publicar(new RecomendacionSolicitada("3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c", 10L, 5));

        assertThat(List.of("tipo", "solicitudId", "productoId", "limite", "fecha"))
                .allMatch(enLaCola::has);
        assertThat(enLaCola.get("tipo").asText()).isEqualTo("RecomendacionSolicitada");

        RecomendacionSolicitada leida = (RecomendacionSolicitada) recibir(enLaCola.toString(), RecomendacionSolicitada.class);
        assertThat(leida.getSolicitudId()).isEqualTo("3f2c9a1e-8b7d-4e2a-9c1f-5d6e7f8a9b0c");
        assertThat(leida.getProductoId()).isEqualTo(10L);
        assertThat(leida.getLimite()).isEqualTo(5);
    }
}
