package com.safelab.platform.reportinganalytics.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Crear metadatos de reporte simulado. No genera PDF ni CSV en v1")
public record ReportCreateRequest(
    @NotBlank @Schema(example="Weekly Sensor Summary") String title,
    @NotBlank @Schema(example="operational") String type) { }
