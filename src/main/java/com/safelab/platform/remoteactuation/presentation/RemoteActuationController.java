package com.safelab.platform.remoteactuation.presentation;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.remoteactuation.presentation.dto.RemoteCommandRequest;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import com.safelab.platform.shared.security.AccessRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/remoteactuation")
@Tag(name="12. Remote Control & Actuation",description="SOLO simulación: no se envían señales a dispositivos reales")
public class RemoteActuationController {
    private final RecordsService r;private final JsonServerGateway db;private final AccessRules access;
    public RemoteActuationController(RecordsService r,JsonServerGateway db,AccessRules access){this.r=r;this.db=db;this.access=access;}
    @GetMapping("/actuators") @Operation(summary="Actuadores asignados")
    public ArrayNode actuators(){return r.list("actuators","actuation.view");}
    @GetMapping("/commands") @Operation(summary="Historial de comandos")
    public ArrayNode commands(){return r.list("remoteCommands","actuation.view");}
    @PostMapping("/actuators/{id}/commands") @Operation(summary="Ejecutar comando simulado y registrar historial")
    public ResponseEntity<ObjectNode> execute(@PathVariable String id,@Valid @RequestBody RemoteCommandRequest request){
        ObjectNode actuator=r.get("actuators",id,"actuation.execute");
        if("blocked".equals(actuator.path("safetyState").asText())||"offline".equals(actuator.path("status").asText()))
            throw new ApiException(HttpStatus.CONFLICT,"ACTUATOR_UNAVAILABLE","El dispositivo está bloqueado o desconectado.");
        ObjectNode patch=db.object();patch.put("lastCommand",request.command());patch.put("running","Start".equals(request.command()));
        r.patch("actuators",id,patch,"actuation.execute");
        ObjectNode command=db.object();command.put("actuatorId",id);command.put("device",actuator.path("name").asText());
        command.put("command",request.command());command.put("status","completed-simulated");
        command.put("requestedBy",access.user().path("fullName").asText());command.put("createdAt",Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(r.create("remoteCommands",command,"actuation.execute"));
    }
}
