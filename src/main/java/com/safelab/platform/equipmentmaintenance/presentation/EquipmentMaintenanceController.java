package com.safelab.platform.equipmentmaintenance.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import io.swagger.v3.oas.annotations.Operation;
import com.safelab.platform.equipmentmaintenance.presentation.dto.MaintenanceCreateRequest;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipmentmaintenance")
@Tag(name="04. Equipment Health & Maintenance")
public class EquipmentMaintenanceController {
    private final RecordsService r;private final JsonServerGateway db;public EquipmentMaintenanceController(RecordsService r,JsonServerGateway db){this.r=r;this.db=db;}
    @GetMapping("/equipment") @Operation(summary="Equipos registrados de la organización")
    public ArrayNode equipment(){return r.list("assets","maintenance.view");}
    @GetMapping("/records") @Operation(summary="Historial de mantenimiento")
    public ArrayNode records(){return r.list("maintenanceRecords","maintenance.view");}
    @GetMapping("/records/{id}") @Operation(summary="Detalle de mantenimiento")
    public ObjectNode detail(@PathVariable String id){return r.get("maintenanceRecords",id,"maintenance.view");}
    @PostMapping("/records") @Operation(summary="Crear orden o registro de mantenimiento")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody MaintenanceCreateRequest request){ObjectNode body=db.object();
        body.put("equipmentId",request.equipmentId());body.put("type",request.type());body.put("status",request.status());
        body.put("date",request.date());body.put("responsible",request.responsible());body.put("notes",request.notes());
        return ResponseEntity.status(HttpStatus.CREATED).body(r.create("maintenanceRecords",body,"maintenance.manage"));
    }
    @PatchMapping("/records/{id}") @Operation(summary="Actualizar trabajo de mantenimiento")
    public ObjectNode patch(@PathVariable String id,@RequestBody ObjectNode body){return r.patch("maintenanceRecords",id,body,"maintenance.manage");}
}
