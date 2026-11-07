package com.safelab.platform.sensormonitoring.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Sensor registrado y vinculado a un equipo de la misma organización")
public record SensorCreateRequest(
    @NotBlank @Schema(example="Temperature Sensor C03") String name,
    @NotBlank @Schema(example="TEMP-C03") String code,
    @NotBlank @Schema(example="temperature") String type,
    @NotBlank @Schema(example="fac-central") String facilityId,
    @NotBlank @Schema(example="asset-001") String assetId,
    @NotBlank @Schema(example="°C") String unit,
    @NotNull @Schema(example="2.0") Double min,
    @NotNull @Schema(example="8.0") Double max) { }
