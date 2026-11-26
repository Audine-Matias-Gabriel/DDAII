package com.ddaii.exceptions;

/** El usuario está autenticado pero no tiene el plan premium habilitado. */
public class AccesoPremiumException extends RuntimeException {

    public AccesoPremiumException(String mensaje) {
        super(mensaje);
    }
}
