package com.ddaii.integrations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoFirmaTest {

    private static final String SECRETO = "secreto-de-prueba";

    private static String firma(String dataId, String requestId, String ts) {
        String manifiesto = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";
        return "ts=" + ts + ",v1=" + MercadoPagoFirma.hmacSha256Hex(SECRETO, manifiesto);
    }

    @Test
    void aceptaUnaFirmaCorrecta() {
        var validador = new MercadoPagoFirma(SECRETO);
        assertThat(validador.esValida(firma("123", "req-1", "1700000000"), "req-1", "123")).isTrue();
    }

    @Test
    void rechazaFirmaAlteradaOtroIdOFaltante() {
        var validador = new MercadoPagoFirma(SECRETO);
        String buena = firma("123", "req-1", "1700000000");
        assertThat(validador.esValida(buena, "req-1", "999")).isFalse();          // otro pago
        assertThat(validador.esValida(buena, "req-2", "123")).isFalse();          // otro request
        assertThat(validador.esValida(buena.replace("v1=", "v1=0"), "req-1", "123")).isFalse();
        assertThat(validador.esValida(null, "req-1", "123")).isFalse();
        assertThat(validador.esValida("basura", "req-1", "123")).isFalse();
    }

    @Test
    void sinClaveConfiguradaNoSeValida() {
        var validador = new MercadoPagoFirma("");
        assertThat(validador.configurada()).isFalse();
        assertThat(validador.esValida(null, null, "123")).isTrue();
    }
}
