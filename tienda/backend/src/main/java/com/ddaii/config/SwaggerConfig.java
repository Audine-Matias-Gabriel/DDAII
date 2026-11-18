package com.ddaii.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuración de Swagger / OpenAPI (springdoc).
 * Swagger UI disponible en /swagger-ui.html (P2, Día 3).
 */
@Configuration
public class SwaggerConfig {

    static final String ESQUEMA_ERROR = "ErrorRespuesta";

    @Bean
    public OpenAPI tiendaOpenAPI() {
        // Habilita el botón "Authorize" de Swagger: se pega el token del login
        // (sin "Bearer") y se envía en Authorization: Bearer <token>.
        Schema<?> error = new ObjectSchema()
                .description("Forma de todas las respuestas de error (GlobalExceptionHandler)")
                .addProperty("estado", new IntegerSchema().example(404))
                .addProperty("error", new StringSchema().example("No encontrado"))
                .addProperty("mensaje", new StringSchema().example("Producto no encontrado: 999999"))
                .addProperty("ruta", new StringSchema().example("/api/productos/999999"));

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer"))
                        .addSchemas(ESQUEMA_ERROR, error))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Tienda - API REST")
                        .version("1.0.0")
                        .description("Catálogo, suscripciones (Mercado Pago), recomendaciones (IA) y similares (Ximilar). "
                                + "Todos los errores responden { estado, error, mensaje, ruta }."));
    }

    /** Agrega a cada endpoint las respuestas de error 400 y 500 con un ejemplo, y 404 a los que reciben un {id}. */
    @Bean
    public OpenApiCustomizer respuestasDeError() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((ruta, item) -> item.readOperations().forEach(op -> {
                var respuestas = op.getResponses();
                respuestas.putIfAbsent("400", error("Solicitud inválida (parámetro, cuerpo o regla de negocio)",
                        400, "Solicitud inválida", "El parámetro 'id' tiene un valor inválido: 'abc' (se esperaba Long)", ruta));
                if (ruta.contains("{")) {
                    respuestas.putIfAbsent("404", error("No existe el recurso", 404, "No encontrado",
                            "Producto no encontrado: 999999", ruta));
                }
                respuestas.putIfAbsent("500", error("Error inesperado (el detalle queda en el log)", 500,
                        "Error del servidor", "Ocurrió un error inesperado. Probá de nuevo más tarde.", ruta));
            }));
        };
    }

    private static ApiResponse error(String descripcion, int estado, String error, String mensaje, String ruta) {
        Example ejemplo = new Example().value(Map.of("estado", estado, "error", error, "mensaje", mensaje, "ruta", ruta));
        return new ApiResponse()
                .description(descripcion)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/" + ESQUEMA_ERROR))
                        .addExamples("ejemplo", ejemplo)));
    }
}
