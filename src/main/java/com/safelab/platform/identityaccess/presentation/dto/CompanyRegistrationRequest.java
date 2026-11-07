package com.safelab.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Registro inicial del gerente. La cuenta queda pendiente de verificación empresarial y pago.")
public record CompanyRegistrationRequest(
    @NotBlank @Size(min=3,max=140) @Schema(example="Laboratorios Nueva Vida S.A.C.") String companyName,
    @NotBlank @Pattern(regexp="[0-9]{11}",message="{validation.ruc}") @Schema(example="20601234567") String ruc,
    @NotBlank @Pattern(regexp="laboratory|pharmaceutical") @Schema(example="laboratory",allowableValues={"laboratory","pharmaceutical"}) String segment,
    @NotBlank @Email @Schema(example="gerencia@empresa.example") String corporateEmail,
    @NotBlank @Pattern(regexp="[\\p{L} ]{2,80}") @Schema(example="Valeria") String firstName,
    @NotBlank @Pattern(regexp="[\\p{L} ]{2,80}") @Schema(example="Chávez") String lastName,
    @NotBlank @Pattern(regexp="[0-9]{9,15}") @Schema(example="945000101") String phone,
    @NotBlank @Email @Schema(example="gerente@empresa.example") String email,
    @NotBlank @Size(min=10) @Schema(example="SafeLab2026%") String password) { }
