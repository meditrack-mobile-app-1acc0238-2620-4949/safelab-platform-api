package com.safelab.platform.remoteactuation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Comando SIMULADO; v1 no envía instrucciones a dispositivos físicos")
public record RemoteCommandRequest(@NotBlank @Pattern(regexp="Start|Stop|Reset") @Schema(example="Start",allowableValues={"Start","Stop","Reset"}) String command) { }
