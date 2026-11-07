package com.safelab.platform.shared.interfaces;

import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@Tag(name="00. Local health")
public class HealthController {
    private final JsonServerGateway data;
    public HealthController(JsonServerGateway data){this.data=data;}
    @GetMapping("/api/v1/health")
    @Operation(summary="Verificar API y JSON Server (sin autenticación)")
    public Map<String,Object> check(){data.all("organizations");return Map.of("status","UP","storage","JSON_SERVER","version","1.0.0");}
}
