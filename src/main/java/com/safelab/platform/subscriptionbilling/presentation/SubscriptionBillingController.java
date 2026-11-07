package com.safelab.platform.subscriptionbilling.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import com.safelab.platform.shared.security.AccessRules;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptionbilling")
@Tag(name="10. Subscription & Billing",description="Consultas de solo lectura: no se cambian planes ni se cobran pagos reales")
public class SubscriptionBillingController {
    private final JsonServerGateway db;private final AccessRules access;
    public SubscriptionBillingController(JsonServerGateway db,AccessRules access){this.db=db;this.access=access;}
    @GetMapping("/subscriptions") @Operation(summary="Todas las suscripciones (solo administrador de SafeLab)")
    public ArrayNode all(){access.platformOnly();return db.all("subscriptions");}
    @GetMapping("/current") @Operation(summary="Suscripción de mi empresa (solo gerente general)")
    public JsonNode current(){access.managerOnly();for(JsonNode item:db.all("subscriptions"))if(access.org().equals(item.path("organizationId").asText()))return item;
        throw new ApiException(HttpStatus.NOT_FOUND,"SUBSCRIPTION_NOT_FOUND","La empresa no tiene una suscripción registrada.");}
    @GetMapping("/organizations/{orgId}/subscription") @Operation(summary="Consultar suscripción por empresa (solo plataforma)")
    public JsonNode byOrg(@PathVariable String orgId){access.platformOnly();for(JsonNode item:db.all("subscriptions"))if(orgId.equals(item.path("organizationId").asText()))return item;
        throw new ApiException(HttpStatus.NOT_FOUND,"SUBSCRIPTION_NOT_FOUND","Suscripción no encontrada.");}
    @GetMapping("/current/invoices") @Operation(summary="Recibos de la empresa del gerente")
    public JsonNode invoices(){return current().path("invoices");}
}
