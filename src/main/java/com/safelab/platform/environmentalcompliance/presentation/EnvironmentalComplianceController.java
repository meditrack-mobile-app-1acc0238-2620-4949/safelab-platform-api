package com.safelab.platform.environmentalcompliance.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import io.swagger.v3.oas.annotations.Operation;
import com.safelab.platform.environmentalcompliance.presentation.dto.RuleCreateRequest;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.domain.ApiException;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/environmentalcompliance")
@Tag(name="11. Environmental Compliance Management")
public class EnvironmentalComplianceController {
    private final RecordsService r;private final DataGateway db;public EnvironmentalComplianceController(RecordsService r,DataGateway db){this.r=r;this.db=db;}
    @GetMapping("/rules") @Operation(summary="Reglas ambientales de la organización")
    public ArrayNode rules(){return r.list("complianceRules","compliance.view");}
    @GetMapping("/evaluations") @Operation(summary="Evaluaciones y lecturas validadas")
    public ArrayNode evaluations(){return r.list("complianceEvaluations","compliance.view");}
    @GetMapping("/violations") @Operation(summary="Historial de incumplimientos")
    public ArrayNode violations(){return r.list("complianceViolations","compliance.view");}
    @PostMapping("/rules") @Operation(summary="Crear regla ambiental")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody RuleCreateRequest request){
        if(request.min()>=request.max())throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_RANGE","min debe ser menor que max.");
        ObjectNode body=db.object();body.put("title",request.title());body.put("type",request.type());body.put("sensorId",request.sensorId());
        body.put("min",request.min());body.put("max",request.max());body.put("status","active");
        return ResponseEntity.status(HttpStatus.CREATED).body(r.create("complianceRules",body,"compliance.manage"));
    }
    @PatchMapping("/rules/{id}") @Operation(summary="Actualizar regla ambiental")
    public ObjectNode update(@PathVariable String id,@RequestBody ObjectNode body){return r.patch("complianceRules",id,body,"compliance.manage");}
}
