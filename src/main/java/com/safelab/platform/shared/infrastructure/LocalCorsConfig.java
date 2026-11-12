package com.safelab.platform.shared.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS limitado a orígenes explícitos. En Render, configurar CORS_ALLOWED_ORIGINS
 * con el dominio real de la web, sin barra final; separados por comas si hay más de uno.
 * En local, por defecto se permiten los puertos usados durante el desarrollo.
 * CORS no sustituye la autenticación ni la autorización del backend.
 */
@Configuration
public class LocalCorsConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;

    public LocalCorsConfig(@Value("${safelab.cors.allowed-origins:}") String configuredOrigins) {
        this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0) return;
        registry.addMapping("/api/v1/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Accept-Language", "Content-Type");
    }
}
