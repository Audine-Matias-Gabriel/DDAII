package com.ddaii.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI (springdoc).
 * Swagger UI disponible en /swagger-ui.html (P2, Día 3).
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tiendaOpenAPI() {
        // Habilita el botón "Authorize" de Swagger: se pega el token del login
        // (sin "Bearer") y se envía en Authorization: Bearer <token>.
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Tienda - API REST")
                        .version("1.0.0")
                        .description("Catálogo, suscripciones (Mercado Pago), recomendaciones (IA) y similares (Ximilar)."));
    }
}
