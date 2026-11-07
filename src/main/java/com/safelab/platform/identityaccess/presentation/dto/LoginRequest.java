package com.safelab.platform.identityaccess.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description="Inicio de sesión de prueba, con el correo o username del seed")
public record LoginRequest(
    @NotBlank @Schema(example="admin@safelab.test") String identifier,
    @NotBlank @Schema(example="SafeLab2026%") String password) { }
