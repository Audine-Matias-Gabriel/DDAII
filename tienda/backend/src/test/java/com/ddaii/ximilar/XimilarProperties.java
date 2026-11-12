package com.ddaii.ximilar;
/*
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "ximilar")
public record XimilarProperties(

        @NotBlank
        String baseUrl,

        // Puede quedar vacío en el perfil "mock".
        // La implementación real (XimilarFacadeImpl) debe fallar rápido si está vacío.
        String apiToken,

        // Idem: obligatorio solo en el perfil real.
        String collectionId,

        @NotNull
        Timeout timeout,

        // Redundante con @Profile("mock"), pero útil como documentación y para logs.
        boolean mock

) {
    public record Timeout(
            @NotNull Duration connect,
            @NotNull Duration read
    ) {}
}

 */