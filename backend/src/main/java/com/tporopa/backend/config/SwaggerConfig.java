package com.tporopa.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI (springdoc).
 * Swagger UI disponible en /swagger-ui.html (P2, Día 3).
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI webropaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Web de Ropa - API REST")
                        .version("1.0.0")
                        .description("Catálogo, suscripciones (Mercado Pago), recomendaciones (IA) y similares (Ximinal)."));
    }
}
