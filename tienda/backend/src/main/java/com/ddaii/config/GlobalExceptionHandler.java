package com.ddaii.config;

import com.ddaii.exceptions.MercadoPagoException;
import com.ddaii.exceptions.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de errores (@RestControllerAdvice).
 *
 * Todas las respuestas de error tienen la misma forma:
 * <pre>{ "estado": 404, "error": "No encontrado", "mensaje": "Producto no encontrado: 9", "ruta": "/api/productos/9" }</pre>
 * - 4xx: error del cliente, con un mensaje que se puede mostrar al usuario.
 * - 5xx: se loguea la excepción completa y al cliente no se le muestra el detalle interno.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String error, String mensaje,
                                                         HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("estado", estado.value());
        body.put("error", error);
        body.put("mensaje", mensaje);
        body.put("ruta", request != null ? request.getRequestURI() : null);
        return ResponseEntity.status(estado).body(body);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> rutaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", "No existe la ruta " + req.getRequestURI(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var campo = ex.getBindingResult().getFieldError();
        String mensaje = campo != null ? campo.getField() + ": " + campo.getDefaultMessage() : "Datos inválidos";
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida", mensaje, req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String esperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "otro tipo";
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "El parámetro '" + ex.getName() + "' tiene un valor inválido: '" + ex.getValue() + "' (se esperaba " + esperado + ")",
                req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> faltaParametro(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "Falta el parámetro '" + ex.getParameterName() + "'", req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> faltaHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida",
                "Falta el header '" + ex.getHeaderName() + "'", req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida", "El cuerpo no es un JSON válido", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoNoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido",
                "La ruta " + req.getRequestURI() + " no acepta " + ex.getMethod(), req);
    }

    /** Errores de negocio (email repetido, credenciales inválidas, ya es premium...). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(IllegalArgumentException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage(), req);
    }

    @ExceptionHandler(MercadoPagoException.class)
    public ResponseEntity<Map<String, Object>> mercadoPago(MercadoPagoException ex, HttpServletRequest req) {
        log.warn("Mercado Pago falló en {}: {}", req.getRequestURI(), ex.getMessage());
        return respuesta(HttpStatus.BAD_GATEWAY, "Mercado Pago no disponible", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {} {}", req.getMethod(), req.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error del servidor",
                "Ocurrió un error inesperado. Probá de nuevo más tarde.", req);
    }
}
