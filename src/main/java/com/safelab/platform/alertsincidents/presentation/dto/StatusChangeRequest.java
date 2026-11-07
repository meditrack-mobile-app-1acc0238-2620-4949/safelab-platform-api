package com.safelab.platform.alertsincidents.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Cambio de estado con justificación")
public record StatusChangeRequest(@NotBlank @Size(min=5,max=500) @Schema(example="Se verificó el equipo y se resolvió la alerta.") String notes) { }
