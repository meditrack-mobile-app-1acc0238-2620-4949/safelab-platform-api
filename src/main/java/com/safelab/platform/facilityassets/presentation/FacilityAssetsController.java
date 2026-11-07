package com.safelab.platform.facilityassets.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import io.swagger.v3.oas.annotations.Operation;
import com.safelab.platform.facilityassets.presentation.dto.AssetCreateRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/facilityassets")
@Tag(name="02. Facility & Asset Management")
public class FacilityAssetsController {
    private final RecordsService records;
    private final DataGateway db;
    private final AccessRules access;
    public FacilityAssetsController(RecordsService records,DataGateway db,AccessRules access){this.records=records;this.db=db;this.access=access;}
    @GetMapping("/organizations")
    @Operation(summary="Consultar organizaciones autorizadas")
    public ArrayNode organizations(){ArrayNode result=db.array();for(JsonNode o:db.all("organizations"))
        if(access.platform() || access.org().equals(o.path("id").asText()))result.add(o);return result;}
    @GetMapping("/facilities")
    @Operation(summary="Consultar sedes de mi empresa")
    public ArrayNode facilities(){return records.list("facilities","assets.view");}
    @GetMapping("/assets")
    @Operation(summary="Listar equipos y activos")
    public ArrayNode assets(){return records.list("assets","assets.view");}
    @GetMapping("/assets/{id}")
    @Operation(summary="Detalle de activo asignado")
    public ObjectNode asset(@PathVariable String id){return records.get("assets",id,"assets.view");}
    @PostMapping("/assets")
    @Operation(summary="Registrar un activo con campos de inventario",description="organizationId es calculado desde la sesión; solo platformAdmin puede especificarlo")
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody AssetCreateRequest request){
        ObjectNode body=db.object();body.put("name",request.name());body.put("category",request.category());
        body.put("facilityId",request.facilityId());body.put("storageUnit",request.storageUnit());
        body.put("location",request.location());body.put("responsible",request.responsible());body.put("status","compliant");
        return ResponseEntity.status(HttpStatus.CREATED).body(records.create("assets",body,"assets.manage"));
    }
    @PatchMapping("/assets/{id}")
    @Operation(summary="Actualizar campos permitidos de un activo")
    public ObjectNode update(@PathVariable String id,@RequestBody ObjectNode body){return records.patch("assets",id,body,"assets.manage");}
    @DeleteMapping("/assets/{id}")
    @Operation(summary="Eliminar activo (solo usuarios con gestión de activos)")
    public ResponseEntity<Void> delete(@PathVariable String id){records.delete("assets",id,"assets.manage");return ResponseEntity.noContent().build();}
}
