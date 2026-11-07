package com.safelab.platform.equipmentmaintenance.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Registro de mantenimiento asociado a un equipo")
public record MaintenanceCreateRequest(
    @NotBlank @Schema(example="asset-001") String equipmentId,
    @NotBlank @Schema(example="preventive") String type,
    @NotBlank @Schema(example="scheduled") String status,
    @NotBlank @Schema(example="2026-11-07") String date,
    @NotBlank @Schema(example="Maintenance Technician") String responsible,
    @Schema(example="Inspection and temperature calibration") String notes) { }
