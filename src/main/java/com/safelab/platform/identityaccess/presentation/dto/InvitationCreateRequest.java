package com.safelab.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description="El gerente reserva un cupo y asigna un rol a una invitación individual")
public record InvitationCreateRequest(
    @NotBlank @Email @Schema(example="nuevo.tecnico@empresa.example") String email,
    @NotBlank @Schema(example="labTechnician") String role,
    List<String> facilityIds,
    List<String> resourceIds) { }
