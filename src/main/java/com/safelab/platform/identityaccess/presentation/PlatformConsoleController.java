package com.safelab.platform.identityaccess.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import com.safelab.platform.shared.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@Tag(name="01. SafeLab Platform Administration",description="Solo administrador global")
public class PlatformConsoleController {
    private final DataGateway db;private final AccessRules a;private final AuthService sessions;
    public PlatformConsoleController(DataGateway db,AccessRules a,AuthService sessions){this.db=db;this.a=a;this.sessions=sessions;}
    @GetMapping("/overview") @Operation(summary="Indicadores globales de SafeLab")
    public ObjectNode overview(){a.platformOnly();ObjectNode result=db.object();
        result.put("organizations",db.all("organizations").size());result.put("users",db.all("users").size());
        result.put("sensors",db.all("sensors").size());result.put("subscriptions",db.all("subscriptions").size());
        result.put("alerts",db.all("alerts").size());return result;}
    @GetMapping("/users") @Operation(summary="Directorio global de usuarios (sin contraseñas)")
    public ArrayNode users(){a.platformOnly();ArrayNode result=db.array();for(JsonNode u:db.all("users"))result.add(sessions.sanitized(u));return result;}
    @GetMapping("/settings") @Operation(summary="Consultar configuración global para la consola de plataforma")
    public ObjectNode settings(){a.platformOnly();ObjectNode out=db.object();
        out.put("name","SafeLab Platform");out.put("supportEmail","admin@safelab.test");
        out.put("timezone","America/Lima");out.put("includedEmployeeSeats",10);
        out.put("managerUsesSeat",false);out.put("invitationHours",72);
        out.put("sessionMinutes",60);return out;}
    @GetMapping("/organizations/{orgId}/overview") @Operation(summary="Resumen de la empresa seleccionada")
    public ObjectNode organization(@PathVariable String orgId){a.platformOnly();JsonNode org=db.get("organizations",orgId);
        ObjectNode result=db.object();result.set("organization",org);for(String coll:new String[]{"users","sensors","assets","alerts","incidents"}){
            int count=0;for(JsonNode item:db.all(coll))if(orgId.equals(item.path("organizationId").asText()))count++;
            result.put(coll,count);
        }return result;
    }
}
