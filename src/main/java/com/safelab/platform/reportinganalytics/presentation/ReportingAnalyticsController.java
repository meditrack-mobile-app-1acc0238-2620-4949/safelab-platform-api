package com.safelab.platform.reportinganalytics.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.DataGateway;
import io.swagger.v3.oas.annotations.Operation;
import com.safelab.platform.reportinganalytics.presentation.dto.ReportCreateRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reportinganalytics")
@Tag(name="07. Reporting & Analytics")
public class ReportingAnalyticsController {
    private final RecordsService r;private final DataGateway db;
    public ReportingAnalyticsController(RecordsService r,DataGateway db){this.r=r;this.db=db;}
    @GetMapping("/reports") @Operation(summary="Reportes disponibles")
    public ArrayNode reports(){return r.list("reports","reports.view");}
    @GetMapping("/summary") @Operation(summary="Resumen de actividad y cobertura")
    public ObjectNode analytics(){ObjectNode o=db.object();o.put("reports",r.list("reports","reports.view").size());
        o.put("sensors",r.list("sensors","sensors.view").size());o.put("alerts",r.list("alerts","incidents.view").size());return o;}
    @PostMapping("/reports") @Operation(summary="Generar registro de reporte simulado (no crea PDF real)")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody ReportCreateRequest input){
        ObjectNode report=db.object();report.put("title",input.title());
        report.put("type",input.type());report.put("format","CSV");report.put("status","ready");
        report.put("generatedAt",Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(r.create("reports",report,"reports.export"));
    }
}
