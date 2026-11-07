package com.safelab.platform.sensormonitoring.presentation.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Medición registrada por un sensor")
public record SensorReadingRequest(@NotNull @Schema(example="8.2") Double value) { }
