package com.safelab.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description="Guardar el conjunto completo de permisos, no una modificación por switch")
public record PermissionUpdateRequest(@NotNull List<String> permissions) { }
