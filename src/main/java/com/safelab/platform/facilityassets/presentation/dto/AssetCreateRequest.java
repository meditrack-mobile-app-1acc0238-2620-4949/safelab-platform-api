package com.safelab.platform.facilityassets.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description="Registrar equipo o activo en una sede de la empresa")
public record AssetCreateRequest(
    @NotBlank @Size(min=3,max=120) @Schema(example="Freezer C-03") String name,
    @NotBlank @Schema(example="Freezer") String category,
    @NotBlank @Schema(example="fac-central") String facilityId,
    @Schema(example="Cold room") String storageUnit,
    @Schema(example="Laboratory 1") String location,
    @Schema(example="Maintenance Technician", description="Texto legado; para asignar acceso use responsibleUserId") String responsible,
    @Schema(description="ID real del empleado responsable, dentro de la misma empresa y sede", example="00000000-0000-4000-8000-000000000102") String responsibleUserId,
    @Schema(example="2026-11-07") String lastInspection,
    @Schema(example="compliant") String status) { }
