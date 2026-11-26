package com.ddaii.exceptions;

/**
 * La imagen subida no produjo ninguna coincidencia válida con el catálogo
 * después de agotar los intentos permitidos (para no seguir gastando créditos).
 */
public class SinCoincidenciasException extends RuntimeException {

    public SinCoincidenciasException(String mensaje) {
        super(mensaje);
    }
}
