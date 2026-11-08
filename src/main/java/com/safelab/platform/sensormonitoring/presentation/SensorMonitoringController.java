package com.safelab.platform.sensormonitoring.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import com.safelab.platform.sensormonitoring.presentation.dto.SensorReadingRequest;
import com.safelab.platform.sensormonitoring.presentation.dto.SensorCreateRequest;
import com.safelab.platform.shared.domain.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/sensormonitoring")
@Tag(name="03. Sensor & Telemetry Monitoring")
public class SensorMonitoringController {
    private final RecordsService records;private final DataGateway db;private final AccessRules access;
    public SensorMonitoringController(RecordsService r,DataGateway d,AccessRules a){records=r;db=d;access=a;}
    @GetMapping("/sensors") @Operation(summary="Consultar sensores y lecturas de recursos asignados")
    public ArrayNode list(){return records.list("sensors","sensors.view");}
    @GetMapping("/sensors/{id}") @Operation(summary="Ver detalle de sensor")
    public ObjectNode detail(@PathVariable String id){return records.get("sensors",id,"sensors.view");}
    @PostMapping("/sensors") @Operation(summary="Registrar sensor")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody SensorCreateRequest request){
        if(request.min()>=request.max())throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_RANGE","min debe ser menor que max.");
        ObjectNode body=db.object();body.put("name",request.name());body.put("code",request.code());body.put("type",request.type());
        body.put("facilityId",request.facilityId());body.put("assetId",request.assetId());body.put("unit",request.unit());
        body.put("min",request.min());body.put("max",request.max());body.put("value",request.min());
        body.put("connection","online");body.put("status","normal");body.put("lastReading",Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(records.create("sensors",body,"sensors.configure"));
    }
    @PatchMapping("/sensors/{id}") @Operation(summary="Configurar sensor")
    public ObjectNode update(@PathVariable String id,@RequestBody ObjectNode body){return records.patch("sensors",id,body,"sensors.configure");}
    @DeleteMapping("/sensors/{id}") @Operation(summary="Eliminar sensor no vinculado a otros registros")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        access.require("sensors.configure");
        var existing=records.get("sensors",id,"sensors.view");
        String[][] dependencies = {{"alerts", "sensorId"}, {"incidents", "relatedSensorId"},
            {"complianceRules", "sensorId"}, {"complianceEvaluations", "sensorId"},
            {"actuators", "linkedSensorId"}};
        for (String[] dependency : dependencies) {
            for (var linked : db.all(dependency[0])) {
                if (id.equals(linked.path(dependency[1]).asText()) &&
                    existing.path("organizationId").asText().equals(linked.path("organizationId").asText()))
                    throw new ApiException(HttpStatus.CONFLICT,"SENSOR_IN_USE",
                        "El sensor tiene registros vinculados ("+dependency[0]+"). Desvincúlalos antes de eliminar.");
            }
        }
        records.delete("sensors", id, "sensors.configure");
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/sensors/{id}/reading") @Operation(summary="Registrar una lectura y calcular si está fuera de rango")
    public ObjectNode reading(@PathVariable String id,@Valid @RequestBody SensorReadingRequest request){
        var sensor=records.get("sensors",id,"sensors.configure");
        ObjectNode patch=db.object();patch.put("value",request.value());patch.put("status",request.value()<sensor.path("min").asDouble(Double.NEGATIVE_INFINITY)
            ||request.value()>sensor.path("max").asDouble(Double.POSITIVE_INFINITY)?"out-of-range":"normal");
        patch.put("connection","online");patch.put("lastReading",Instant.now().toString());
        ObjectNode updated=records.patch("sensors",id,patch,"sensors.configure");
        if("out-of-range".equals(updated.path("status").asText())){
            ObjectNode alert=db.object();alert.put("title","Sensor out of range: "+updated.path("name").asText());
            alert.put("type","temperature");alert.put("message","Measurement outside configured threshold.");
            alert.put("sensorId",id);alert.put("assetId",updated.path("assetId").asText());alert.put("facilityId",updated.path("facilityId").asText());
            alert.put("severity","warning");alert.put("status","active");
            records.create("alerts",alert,"sensors.configure");
        }
        return updated;
    }
}
