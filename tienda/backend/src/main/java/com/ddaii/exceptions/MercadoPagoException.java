package com.ddaii.exceptions;

/**
 * Falla al hablar con Mercado Pago: token sin configurar, API caída o
 * respuesta de error.
 */
public class MercadoPagoException extends RuntimeException {

    public MercadoPagoException(String mensaje) {
        super(mensaje);
    }

    public MercadoPagoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
