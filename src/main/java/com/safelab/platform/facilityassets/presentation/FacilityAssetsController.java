package com.safelab.platform.facilityassets.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.facilityassets.presentation.dto.AssetCreateRequest;
import com.safelab.platform.shared.application.RecordsService;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.Set;

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

    @GetMapping("/assignable-users")
    @Operation(summary="Personal activo autorizado para asignar responsables de activos",
        description="Solo gerente de la empresa; devuelve ID y nombre para selects sin exponer contraseñas ni usuarios de otra empresa.")
    public ArrayNode assignableUsers(){
        access.managerOnly();
        ArrayNode result=db.array();
        for(JsonNode u:db.all("users")) {
            if(!access.org().equals(u.path("organizationId").asText()) ||
                !"active".equalsIgnoreCase(u.path("status").asText()) ||
                "organizationAdmin".equals(u.path("role").asText()) ||
                "platformAdmin".equals(u.path("role").asText())) continue;
            ObjectNode employee=db.object();
            employee.put("id",u.path("id").asText());
            employee.put("fullName",u.path("fullName").asText());
            employee.put("role",u.path("role").asText());
            employee.set("facilityIds",u.path("facilityIds").deepCopy());
            result.add(employee);
        }
        return result;
    }

    @PostMapping("/assets")
    @Operation(summary="Registrar un activo y asignar responsable por ID",
        description="responsibleUserId vincula un empleado existente y actualiza su alcance de recursos. organizationId se obtiene de la sesion.")
    @Transactional
    public ResponseEntity<ObjectNode> create(@Valid @RequestBody AssetCreateRequest request){
        ObjectNode body=db.object();body.put("name",request.name());body.put("category",request.category());
        body.put("facilityId",request.facilityId());body.put("storageUnit",request.storageUnit());
        body.put("location",request.location());body.put("status",validatedStatus(request.status()));
        if(request.lastInspection()!=null) body.put("lastInspection",request.lastInspection());
        String targetUser=normalized(request.responsibleUserId());
        if(targetUser != null) {
            String org=access.org();
            JsonNode employee=validEmployee(org,targetUser,request.facilityId());
            body.put("responsibleUserId",targetUser);
            body.put("responsible",employee.path("fullName").asText());
        } else {
            // Compatibilidad con las integraciones v2.0, pero texto no concede accesos.
            body.put("responsible",request.responsible()==null?"":request.responsible());
        }
        ObjectNode created=records.create("assets",body,"assets.manage");
        if(targetUser!=null) changeAssignment(null,targetUser,created.path("id").asText());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/assets/{id}")
    @Operation(summary="Actualizar activo; cambiar responsable por ID sincroniza recursos del empleado")
    @Transactional
    public ObjectNode update(@PathVariable String id,@RequestBody ObjectNode body){
        JsonNode previous=db.get("assets",id);
        if(body.has("status")) body.put("status",validatedStatus(body.path("status").asText()));
        // El gerente controla las asignaciones; el texto libre nunca concede acceso.
        if(body.has("responsibleUserId")) {
            access.managerOnly();
            String newUser=normalized(body.path("responsibleUserId").asText(null));
            String org=previous.path("organizationId").asText();
            access.sameOrg(org);
            JsonNode employee=newUser==null?null:validEmployee(org,newUser,
                body.hasNonNull("facilityId")?body.path("facilityId").asText():previous.path("facilityId").asText());
            if(employee==null) { body.putNull("responsibleUserId"); body.put("responsible",""); }
            else { body.put("responsibleUserId",newUser);body.put("responsible",employee.path("fullName").asText()); }
        } else if(body.has("responsible") && previous.hasNonNull("responsibleUserId")) {
            // Un cliente antiguo no puede desincronizar una asignacion vinculada.
            if(!previous.path("responsible").asText().equals(body.path("responsible").asText()))
                throw new ApiException(HttpStatus.BAD_REQUEST,"RESPONSIBLE_ID_REQUIRED","Selecciona un empleado por responsibleUserId.");
            body.remove("responsible");
        }
        if(body.has("facilityId") && !body.has("responsibleUserId") && previous.hasNonNull("responsibleUserId"))
            validEmployee(previous.path("organizationId").asText(),previous.path("responsibleUserId").asText(),body.path("facilityId").asText());
        String oldUser=normalized(previous.path("responsibleUserId").asText(null));
        ObjectNode updated=records.patch("assets",id,body,"assets.manage");
        if(body.has("responsibleUserId")) changeAssignment(oldUser,normalized(updated.path("responsibleUserId").asText(null)),id);
        return updated;
    }

    @DeleteMapping("/assets/{id}")
    @Operation(summary="Eliminar activo y limpiar sus referencias de asignacion")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String id){
        access.require("assets.manage");
        JsonNode existing=records.get("assets",id,"assets.view");
        // PostgreSQL FK relationships must remain consistent: never cascade-delete clinical records.
        String[][] dependencies = {{"sensors", "assetId"}, {"maintenanceRecords", "equipmentId"},
            {"alerts", "assetId"}, {"incidents", "relatedAssetId"}, {"actuators", "assetId"}};
        for (String[] dependency : dependencies) {
            for (JsonNode linked : db.all(dependency[0])) {
                if (id.equals(linked.path(dependency[1]).asText()) &&
                    existing.path("organizationId").asText().equals(linked.path("organizationId").asText()))
                    throw new ApiException(HttpStatus.CONFLICT,"ASSET_IN_USE",
                        "El activo tiene registros vinculados ("+dependency[0]+"). Desvincúlalos antes de eliminar.");
            }
        }
        records.delete("assets",id,"assets.manage");
        for(JsonNode user:db.all("users")) if(existing.path("organizationId").asText().equals(user.path("organizationId").asText())) {
            Set<String> ids=ids(user.path("resourceIds"));
            if(ids.remove(id)) patchResourceIds(user.path("id").asText(),ids);
        }
        return ResponseEntity.noContent().build();
    }

    private JsonNode validEmployee(String org,String userId,String facilityId){
        // Un empleado con assets.manage puede vincularse SOLO a si mismo al crear.
        if (!access.manager() && !access.user().path("id").asText().equals(userId)) access.denied();
        JsonNode employee=db.get("users",userId);
        if(!org.equals(employee.path("organizationId").asText())) access.denied();
        if(!"active".equalsIgnoreCase(employee.path("status").asText()) ||
            "organizationAdmin".equals(employee.path("role").asText()) ||
            "platformAdmin".equals(employee.path("role").asText()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_RESPONSIBLE","Debes elegir un empleado activo de la empresa.");
        JsonNode authorized=employee.path("facilityIds");
        if(authorized.isArray() && authorized.size()>0) {
            boolean found=false; for(JsonNode allowed:authorized) if(facilityId.equals(allowed.asText())) {found=true;break;}
            if(!found) throw new ApiException(HttpStatus.BAD_REQUEST,"UNASSIGNED_FACILITY","El empleado no tiene asignada la sede del activo.");
        }
        return employee;
    }

    private void changeAssignment(String previousUser,String nextUser,String assetId){
        if(previousUser!=null && !previousUser.equals(nextUser)){
            JsonNode user=db.get("users",previousUser);
            Set<String> resources=ids(user.path("resourceIds"));
            if(resources.remove(assetId)) patchResourceIds(previousUser,resources);
        }
        if(nextUser!=null){
            JsonNode user=db.get("users",nextUser);
            Set<String> resources=ids(user.path("resourceIds"));
            if(resources.add(assetId)) patchResourceIds(nextUser,resources);
        }
    }

    private void patchResourceIds(String userId,Set<String> resourceIds){
        ArrayNode list=db.array();resourceIds.forEach(list::add);
        ObjectNode patch=db.object();patch.set("resourceIds",list);
        db.patch("users",userId,patch);
    }
    private static Set<String> ids(JsonNode entries){
        Set<String> result=new LinkedHashSet<>();
        if(entries.isArray()) for(JsonNode id:entries) result.add(id.asText());
        return result;
    }
    private static String validatedStatus(String value){
        String status=value==null||value.isBlank()?"compliant":value.trim().toLowerCase();
        if(!Set.of("compliant","warning","critical","maintenance").contains(status))
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_ASSET_STATUS","Estado de activo desconocido.");
        return status;
    }
    private static String normalized(String value){return value==null||value.isBlank()?null:value.trim();}
}
