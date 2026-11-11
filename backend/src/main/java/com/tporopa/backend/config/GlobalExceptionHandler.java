package com.tporopa.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo global de errores (@RestControllerAdvice, P2 Día 2).
 * Devuelve un error de ejemplo en Swagger cuando algo falla.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Solicitud inválida");
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarRuntime(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error del servidor");
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarGeneral(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error inesperado");
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
