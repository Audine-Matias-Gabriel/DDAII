package com.ddaii.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS global para permitir que el frontend llame al backend (P2, Día 3).
 * Orígenes configurables por variable de entorno CORS_ORIGINS.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors-origins:http://localhost:5173}")
    private String[] corsOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
