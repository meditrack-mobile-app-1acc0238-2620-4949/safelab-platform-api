package com.safelab.platform.environmentalcompliance.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Rango de conservación aplicable a un sensor")
public record RuleCreateRequest(
    @NotBlank @Schema(example="Conservation temperature") String title,
    @NotBlank @Schema(example="temperature") String type,
    @NotBlank @Schema(example="sen-001") String sensorId,
    @NotNull @Schema(example="2") Double min,
    @NotNull @Schema(example="8") Double max) { }
