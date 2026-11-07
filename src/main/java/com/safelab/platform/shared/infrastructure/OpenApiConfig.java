package com.safelab.platform.shared.infrastructure;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Locale;

/**
 * Dos vistas de documentación para exactamente los mismos endpoints.
 * El selector de Swagger cambia el texto, pero NO cambia las rutas ni los JSON.
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("SafeLab Platform API").version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer")
                .bearerFormat("Opaque mock session token")));
    }

    @Bean
    public GroupedOpenApi spanishOpenApi(MessageSource messages) {
        return GroupedOpenApi.builder().group("es").pathsToMatch("/api/v1/**")
            .addOpenApiCustomizer(new LocalizedOpenApiCustomizer(messages, Locale.forLanguageTag("es")))
            .build();
    }

    @Bean
    public GroupedOpenApi englishOpenApi(MessageSource messages) {
        return GroupedOpenApi.builder().group("en").pathsToMatch("/api/v1/**")
            .addOpenApiCustomizer(new LocalizedOpenApiCustomizer(messages, Locale.ENGLISH))
            .build();
    }
}
