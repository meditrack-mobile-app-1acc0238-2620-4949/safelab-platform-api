package com.safelab.platform.alertsincidents.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.alertsincidents.presentation.dto.StatusChangeRequest;
import com.safelab.platform.alertsincidents.presentation.dto.IncidentCreateRequest;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.DataGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/alertsincidents")
@Tag(name="05. Alert & Incident Management")
public class AlertsIncidentsController {
    private final RecordsService r;private final DataGateway db;
    public AlertsIncidentsController(RecordsService r,DataGateway db){this.r=r;this.db=db;}
    @GetMapping("/alerts") @Operation(summary="Alertas autorizadas")
    public ArrayNode alerts(){return r.list("alerts","incidents.view");}
    @GetMapping("/notifications") @Operation(summary="Notificaciones empresariales")
    public ArrayNode notifications(){return r.list("notifications","incidents.view");}
    @GetMapping("/incidents") @Operation(summary="Incidentes de la organización")
    public ArrayNode incidents(){return r.list("incidents","incidents.view");}
    @GetMapping("/incidents/{id}") @Operation(summary="Detalle de incidente")
    public ObjectNode incident(@PathVariable String id){return r.get("incidents",id,"incidents.view");}
    @PostMapping("/incidents") @Operation(summary="Registrar incidente")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody IncidentCreateRequest request){ObjectNode body=db.object();
        body.put("title",request.title());body.put("description",request.description());body.put("facilityId",request.facilityId());
        body.put("severity",request.severity());body.put("status","open");body.put("relatedSensorId",request.relatedSensorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(r.create("incidents",body,"incidents.create"));
    }
    @PostMapping("/alerts/{id}/acknowledge") @Operation(summary="Reconocer alerta")
    public ObjectNode acknowledge(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){return change("alerts",id,"acknowledged",request.notes(),"alerts.acknowledge");}
    @PostMapping("/alerts/{id}/resolve") @Operation(summary="Resolver alerta")
    public ObjectNode resolveAlert(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){return change("alerts",id,"resolved",request.notes(),"incidents.resolve");}
    @PostMapping("/incidents/{id}/resolve") @Operation(summary="Marcar incidente como resuelto")
    public ObjectNode resolveIncident(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){return change("incidents",id,"resolved",request.notes(),"incidents.resolve");}
    @PostMapping("/alerts/{id}/escalate") @Operation(summary="Escalar alerta y abrir incidente vinculado")
    @Transactional
    public ObjectNode escalate(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){
        var alert=r.get("alerts",id,"incidents.create");
        ObjectNode incident=db.object();incident.put("title","Escalation: "+alert.path("title").asText());
        incident.put("description",request.notes());incident.put("facilityId",alert.path("facilityId").asText());
        incident.put("relatedAlertId",id);incident.put("relatedSensorId",alert.path("sensorId").asText());
        incident.put("severity",alert.path("severity").asText());incident.put("status","open");
        ObjectNode created=r.create("incidents",incident,"incidents.create");
        ObjectNode patch=db.object();patch.put("status","escalated");
        r.patch("alerts",id,patch,"incidents.create");
        return created;
    }
    @PostMapping("/notifications/{id}/mark-read") @Operation(summary="Marcar notificación como leída")
    public ObjectNode markRead(@PathVariable String id){ObjectNode patch=db.object();patch.put("unread",false);
        return r.patch("notifications",id,patch,"incidents.view");}
    @PostMapping("/incidents/{id}/investigate") @Operation(summary="Comenzar investigación")
    public ObjectNode investigate(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){
        return change("incidents",id,"investigating",request.notes(),"incidents.resolve");}
    @PostMapping("/incidents/{id}/close") @Operation(summary="Cerrar caso resuelto")
    public ObjectNode close(@PathVariable String id,@Valid @RequestBody StatusChangeRequest request){
        ObjectNode incident=r.get("incidents",id,"incidents.resolve");
        if(!"resolved".equals(incident.path("status").asText()))
            throw new com.safelab.platform.shared.domain.ApiException(org.springframework.http.HttpStatus.CONFLICT,
                "INCIDENT_NOT_RESOLVED","Debes resolver el incidente antes de cerrarlo.");
        return change("incidents",id,"closed",request.notes(),"incidents.resolve");
    }
    private ObjectNode change(String collection,String id,String status,String notes,String perm){
        r.get(collection,id,perm);ObjectNode patch=db.object();patch.put("status",status);patch.put("notes",notes);
        patch.put("updatedAt",Instant.now().toString());return r.patch(collection,id,patch,perm);
    }
}
