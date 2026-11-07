package com.safelab.platform.shared.interfaces;

import com.safelab.platform.shared.domain.DataGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@Tag(name="00. Local health")
public class HealthController {
    private final DataGateway data;
    private final String mode;
    public HealthController(DataGateway data, @Value("${safelab.storage-mode}") String mode) {
        this.data = data; this.mode = mode;
    }
    @GetMapping("/api/v1/health")
    @Operation(summary="Verificar API y fuente de datos (sin autenticacion)")
    public Map<String,Object> check() {
        data.all("organizations");
        return Map.of("status", "UP", "storage", mode, "version", "2.0.0");
    }
}
