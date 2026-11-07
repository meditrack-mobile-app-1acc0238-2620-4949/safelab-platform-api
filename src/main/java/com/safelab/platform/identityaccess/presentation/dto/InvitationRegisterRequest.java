package com.safelab.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Aceptar invitación; la cuenta queda pendiente de confirmar el correo")
public record InvitationRegisterRequest(
    @NotBlank @Schema(example="ABCD1234EFGH5678") String code,
    @NotBlank @Pattern(regexp="[\\p{L} ]{2,80}") String firstName,
    @NotBlank @Pattern(regexp="[\\p{L} ]{2,80}") String lastName,
    @NotBlank @Pattern(regexp="[0-9]{9,15}") String phone,
    @NotBlank @Size(min=10) String password) { }
