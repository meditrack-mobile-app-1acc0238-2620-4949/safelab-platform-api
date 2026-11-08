package com.safelab.platform.identityaccess.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.identityaccess.presentation.dto.*;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import com.safelab.platform.shared.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identityaccess")
@Tag(name="01. Identity & Access Management",description="Empleados, invitaciones y permisos por empresa")
public class OrganizationUsersController {
    private final DataGateway db;
    private final AccessRules access;
    private final AuthService session;
    public OrganizationUsersController(DataGateway db,AccessRules access,AuthService session){this.db=db;this.access=access;this.session=session;}

    @GetMapping("/permissions/catalog")
    @Operation(summary="Catálogo de los 21 permisos y defaults por rol")
    public JsonNode catalog(){return access.catalog();}

    @GetMapping("/organizations/{orgId}/users")
    @Operation(summary="Listar empleados de una empresa, sin contraseñas")
    public ArrayNode employees(@PathVariable String orgId){
        access.sameOrg(orgId);
        if(!access.platform()&&!access.manager())access.require("users.view");
        ArrayNode result=db.array();for(JsonNode u:db.all("users"))if(orgId.equals(u.path("organizationId").asText()))result.add(session.sanitized(u));
        return result;
    }
    @GetMapping("/organizations/{orgId}/invitations")
    @Operation(summary="Consultar invitaciones pendientes y utilizadas")
    public ArrayNode invitations(@PathVariable String orgId){access.sameOrg(orgId);if(!access.platform())access.managerOnly();
        ArrayNode result=db.array();for(JsonNode i:db.all("invitations"))if(orgId.equals(i.path("organizationId").asText()))result.add(i);return result;
    }
    @PostMapping("/organizations/{orgId}/invitations")
    @Operation(summary="Invitar a un empleado reservando un cupo",description="Solo gerente de esa empresa. El código se devuelve para pruebas; no se envían correos reales.")
    @Transactional
    public ResponseEntity<ObjectNode> invite(@PathVariable String orgId,@Valid @RequestBody InvitationCreateRequest request){
        access.managerOnly();access.sameOrg(orgId);
        if(!access.catalog().path("defaults").has(request.role()))throw new ApiException(HttpStatus.BAD_REQUEST,"ROLE_NOT_ALLOWED","Este rol no está disponible para invitaciones.");
        for(JsonNode u:db.all("users"))if(request.email().equalsIgnoreCase(u.path("email").asText()))
            throw new ApiException(HttpStatus.CONFLICT,"EMAIL_ALREADY_REGISTERED","Este correo ya tiene una cuenta.");
        for(JsonNode inv:db.all("invitations"))if(request.email().equalsIgnoreCase(inv.path("email").asText())&&"pending".equals(inv.path("status").asText()))
            throw new ApiException(HttpStatus.CONFLICT,"INVITATION_EXISTS","Ya existe una invitación pendiente para este correo.");
        int capacity=0;boolean active=false;
        for(JsonNode sub:db.all("subscriptions"))if(orgId.equals(sub.path("organizationId").asText())){
            active="active".equals(sub.path("status").asText());capacity=sub.path("includedSeats").asInt()+sub.path("additionalSeats").asInt();break;
        }
        if(!active)throw new ApiException(HttpStatus.CONFLICT,"SUBSCRIPTION_INACTIVE","La empresa necesita una suscripción activa.");
        int used=0;for(JsonNode u:db.all("users"))if(orgId.equals(u.path("organizationId").asText())&& !"organizationAdmin".equals(u.path("role").asText()))used++;
        for(JsonNode inv:db.all("invitations"))if(orgId.equals(inv.path("organizationId").asText())&&"pending".equals(inv.path("status").asText()))used++;
        if(used>=capacity)throw new ApiException(HttpStatus.CONFLICT,"NO_AVAILABLE_SEATS","La empresa no tiene cupos de empleados disponibles.");
        ObjectNode inv=db.object();inv.put("id",UUID.randomUUID().toString());inv.put("code",newCode());
        inv.put("email",request.email().toLowerCase());inv.put("role",request.role());inv.put("organizationId",orgId);
        inv.put("status","pending");inv.put("expiresAt",Instant.now().plus(72,ChronoUnit.HOURS).toString());
        inv.set("facilityIds",db.array());inv.set("resourceIds",db.array());
        if(request.facilityIds()!=null)request.facilityIds().forEach(x->((ArrayNode)inv.get("facilityIds")).add(x));
        if(request.resourceIds()!=null)request.resourceIds().forEach(x->((ArrayNode)inv.get("resourceIds")).add(x));
        validateResources(orgId,request.facilityIds(),request.resourceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(db.create("invitations",inv));
    }
    @PostMapping("/organizations/{orgId}/invitations/{id}/cancel")
    @Operation(summary="Cancelar una invitación pendiente")
    @Transactional
    public ObjectNode cancel(@PathVariable String orgId,@PathVariable String id){access.managerOnly();access.sameOrg(orgId);
        JsonNode inv=db.get("invitations",id);if(!orgId.equals(inv.path("organizationId").asText()))access.denied();
        if(!"pending".equals(inv.path("status").asText()))throw new ApiException(HttpStatus.CONFLICT,"INVITATION_NOT_PENDING","Solo se pueden cancelar invitaciones pendientes.");
        ObjectNode p=db.object();p.put("status","cancelled");return db.patch("invitations",id,p);
    }
    @GetMapping("/organizations/{orgId}/users/{userId}/permissions")
    @Operation(summary="Consultar permisos efectivos de un empleado")
    public ObjectNode permissions(@PathVariable String orgId,@PathVariable String userId){
        access.sameOrg(orgId);if(!access.platform()&&!access.manager())access.denied();
        JsonNode u=member(orgId,userId);ObjectNode result=db.object();result.put("userId",userId);result.put("role",u.path("role").asText());
        JsonNode perms=access.catalog().path("defaults").path(u.path("role").asText());
        for(JsonNode override:db.all("permissionAssignments"))if(userId.equals(override.path("userId").asText())){perms=override.path("permissions");break;}
        result.set("permissions",perms.deepCopy());return result;
    }
    @PutMapping("/organizations/{orgId}/users/{userId}/permissions")
    @Operation(summary="Guardar permisos (todos los switches en una sola operación)")
    @Transactional
    public ObjectNode updatePermissions(@PathVariable String orgId,@PathVariable String userId,@Valid @RequestBody PermissionUpdateRequest request){
        access.managerOnly();access.sameOrg(orgId);JsonNode member=member(orgId,userId);
        if("organizationAdmin".equals(member.path("role").asText()) || "platformAdmin".equals(member.path("role").asText()))access.denied();
        Set<String> valid=new HashSet<>();for(JsonNode group:access.catalog().path("groups"))for(JsonNode p:group.path("permissions"))valid.add(p.asText());
        if(request.permissions().size()>valid.size()||new HashSet<>(request.permissions()).size()!=request.permissions().size() ||!valid.containsAll(request.permissions()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_PERMISSIONS","Los permisos contienen claves duplicadas o desconocidas.");
        ArrayNode array=db.array();request.permissions().forEach(array::add);
        for(JsonNode assign:db.all("permissionAssignments"))if(userId.equals(assign.path("userId").asText())){
            ObjectNode patch=db.object();patch.set("permissions",array);return db.patch("permissionAssignments",assign.path("id").asText(),patch);
        }
        ObjectNode item=db.object();item.put("id",UUID.randomUUID().toString());item.put("userId",userId);item.put("organizationId",orgId);
        item.set("permissions",array);return db.create("permissionAssignments",item);
    }
    @PatchMapping("/organizations/{orgId}/users/{userId}/assignments")
    @Operation(summary="Actualizar sedes y recursos asignados a un empleado")
    @Transactional
    public ObjectNode assignments(@PathVariable String orgId,@PathVariable String userId,@RequestBody ObjectNode payload){
        access.managerOnly(); access.sameOrg(orgId);
        JsonNode user = member(orgId,userId);
        if ("organizationAdmin".equals(user.path("role").asText())) access.denied();
        if (!payload.path("facilityIds").isArray() || !payload.path("resourceIds").isArray())
            throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_ASSIGNMENTS","Se requieren listas facilityIds y resourceIds.");
        java.util.List<String> facilities = new java.util.ArrayList<>();
        java.util.List<String> resources = new java.util.ArrayList<>();
        payload.path("facilityIds").forEach(item -> facilities.add(item.asText()));
        payload.path("resourceIds").forEach(item -> resources.add(item.asText()));
        validateResources(orgId,facilities,resources);
        ObjectNode patch=db.object(); patch.set("facilityIds",payload.get("facilityIds"));
        patch.set("resourceIds",payload.get("resourceIds"));
        return session.sanitized(db.patch("users",userId,patch));
    }

    private JsonNode member(String orgId,String userId){JsonNode u=db.get("users",userId);if(!orgId.equals(u.path("organizationId").asText()))access.denied();return u;}
    private String newCode(){SecureRandom rand=new SecureRandom();String chars="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder s=new StringBuilder();for(int i=0;i<24;i++)s.append(chars.charAt(rand.nextInt(chars.length())));return s.toString();}
    private void validateResources(String orgId,java.util.List<String> facilities,java.util.List<String> resources){
        if(facilities!=null)for(String id:facilities)checkResource("facilities",id,orgId);
        if(resources!=null)for(String id:resources){String collection=id.startsWith("sen-")?"sensors":"assets";checkResource(collection,id,orgId);}
    }
    private void checkResource(String collection,String id,String orgId){JsonNode value=db.get(collection,id);if(!orgId.equals(value.path("organizationId").asText()))access.denied();}
}
