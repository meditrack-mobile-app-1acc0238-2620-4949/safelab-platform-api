package com.safelab.platform.shared.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite probar las dos APIs LOCALES desde una misma pagina Swagger.
 *  Nunca se reutiliza esta lista de origenes como configuracion productiva.
 */
@Configuration
public class LocalCorsConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
            .allowedOrigins("http://localhost:8080", "http://localhost:8081",
                            "http://127.0.0.1:8080", "http://127.0.0.1:8081")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Accept-Language", "Content-Type");
    }
}
