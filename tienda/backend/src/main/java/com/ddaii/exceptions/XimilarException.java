package com.ddaii.exceptions;

/** Falla al hablar con Ximilar: credenciales ausentes, API caída o respuesta inválida. */
public class XimilarException extends RuntimeException {

    public XimilarException(String mensaje) {
        super(mensaje);
    }

    public XimilarException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
