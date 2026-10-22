package com.ddaii.events;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import tools.jackson.databind.ObjectMapper;

class PedidoCreadoConsumerTest {

    private PedidoCreadoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PedidoCreadoConsumer(new ObjectMapper());
    }

    private String mensajeValido() {
        return """
                {
                  "pedidoId": 7,
                  "items": [
                    {
                      "productoId": 1,
                      "nombre": "Remera Nike",
                      "genero": "HOMBRE",
                      "talle": "M",
                      "cantidad": 2
                    }
                  ]
                }
                """;
    }

    @Test
    void consumir_mensajeValido_noFalla() {
        assertThatCode(() -> consumer.consumir(mensajeValido()))
                .doesNotThrowAnyException();
    }

    @Test
    void consumir_jsonInvalido_rechazaElMensaje() {
        assertThatThrownBy(() -> consumer.consumir("{esto no es json"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Test
    void consumir_jsonSinItems_rechazaElMensaje() {
        assertThatThrownBy(() -> consumer.consumir("{\"pedidoId\": 7}"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
