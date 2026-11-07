package com.safelab.platform.audittraceability.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.DataGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audittraceability")
@Tag(name="08. Audit & Traceability")
public class AuditTraceabilityController {
    private final RecordsService r;private final DataGateway db;
    public AuditTraceabilityController(RecordsService r,DataGateway db){this.r=r;this.db=db;}
    @GetMapping("/logs") @Operation(summary="Registros de auditoría del alcance autorizado")
    public ArrayNode logs(){return r.list("auditLogs","audit.view");}
    @GetMapping("/timeline") @Operation(summary="Resumen de auditoría por empresa")
    public ObjectNode timeline(){ArrayNode logs=r.list("auditLogs","audit.view");ObjectNode result=db.object();
        result.put("total",logs.size());long pending=0;for(var event:logs)if("pending".equals(event.path("status").asText()))pending++;
        result.put("pending",pending);result.set("events",logs);return result;}
}
