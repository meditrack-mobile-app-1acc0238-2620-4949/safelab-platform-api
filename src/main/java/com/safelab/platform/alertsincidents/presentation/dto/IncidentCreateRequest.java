package com.safelab.platform.alertsincidents.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Registro inicial de incidente")
public record IncidentCreateRequest(
    @NotBlank @Schema(example="Possible cold-chain loss") String title,
    @NotBlank @Size(min=8,max=500) @Schema(example="The freezer reported readings above threshold") String description,
    @NotBlank @Schema(example="fac-central") String facilityId,
    @NotBlank @Schema(example="warning") String severity,
    @Schema(example="sen-001") String relatedSensorId) { }
