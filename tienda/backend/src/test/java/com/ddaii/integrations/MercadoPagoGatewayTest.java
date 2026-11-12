package com.ddaii.integrations;

import com.ddaii.exceptions.MercadoPagoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * No llama a Mercado Pago: solo comprueba lo que pasa antes de la red. El
 * pago real se prueba a mano con el token de la cuenta de prueba.
 */
class MercadoPagoGatewayTest {

    private static final BigDecimal PRECIO = new BigDecimal("5000");

    @Test
    void sinAccessTokenFallaConUnMensajeClaro() {
        MercadoPagoGateway gateway =
                new MercadoPagoGateway("", PRECIO, "http://localhost:5173");

        assertThatThrownBy(() -> gateway.crearPreferenciaPremium(1L))
                .isInstanceOf(MercadoPagoException.class)
                .hasMessageContaining("MP_ACCESS_TOKEN");
    }

    @Test
    void conAccessTokenNuloFallaIgual() {
        MercadoPagoGateway gateway =
                new MercadoPagoGateway(null, PRECIO, "http://localhost:5173");

        assertThatThrownBy(() -> gateway.crearPreferenciaPremium(1L))
                .isInstanceOf(MercadoPagoException.class)
                .hasMessageContaining("MP_ACCESS_TOKEN");
    }
}
