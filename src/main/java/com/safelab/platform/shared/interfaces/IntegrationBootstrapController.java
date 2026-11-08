package com.safelab.platform.shared.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.DataGateway;
import com.safelab.platform.shared.security.AccessRules;
import com.safelab.platform.shared.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Snapshot autorizado para las aplicaciones Web y Android.
 * No expone contraseñas, tokens ni una colección global sin filtrar.
 * Las escrituras siguen pasando por los controladores de cada BC.
 */
@RestController
@RequestMapping("/api/v1/integration")
@Tag(name="13. Frontend Integration", description="Lectura inicial multiempresa para Web y Mobile")
public class IntegrationBootstrapController {
    private final DataGateway data;
    private final AccessRules access;
    private final AuthService sessions;

    public IntegrationBootstrapController(DataGateway data, AccessRules access, AuthService sessions) {
        this.data = data; this.access = access; this.sessions = sessions;
    }

    private static final String[][] COLLECTIONS = {
        {"facilities", "assets.view"}, {"assets", "assets.view"},
        {"sensors", "sensors.view"}, {"alerts", "incidents.view"},
        {"notifications", "incidents.view"}, {"incidents", "incidents.view"},
        {"maintenanceRecords", "maintenance.view"}, {"actuators", "actuation.view"},
        {"remoteCommands", "actuation.view"}, {"reports", "reports.view"},
        {"auditLogs", "audit.view"}, {"complianceRules", "compliance.view"},
        {"complianceEvaluations", "compliance.view"}, {"complianceViolations", "compliance.view"}
    };

    @GetMapping("/bootstrap")
    @Operation(summary="Datos autorizados para Web y Mobile",
        description="Agrupa colecciones permitidas de los 12 BC para inicializar las pantallas. No autoriza escrituras genéricas.")
    public ObjectNode bootstrap() {
        JsonNode current = sessions.current();
        ObjectNode out = data.object();
        ObjectNode currentUser = sessions.sanitized(current);
        ArrayNode effectivePermissions = data.array();
        for (JsonNode group : access.catalog().path("groups")) {
            for (JsonNode permission : group.path("permissions")) {
                if (access.allowed(permission.asText())) effectivePermissions.add(permission.asText());
            }
        }
        if (access.allowed("billing.read")) effectivePermissions.add("billing.read");
        currentUser.set("effectivePermissions", effectivePermissions);
        out.set("currentUser", currentUser);
        String org = access.org();

        ArrayNode organizations = data.array();
        for (JsonNode organization : data.all("organizations"))
            if (access.platform() || org.equals(organization.path("id").asText()))
                organizations.add(organization.deepCopy());
        out.set("organizations", organizations);

        ArrayNode users = data.array();
        for (JsonNode user : data.all("users")) {
            boolean visible = access.platform() || (access.manager() && org.equals(user.path("organizationId").asText()))
                || current.path("id").asText().equals(user.path("id").asText());
            if (visible) {
                ObjectNode safeUser = sessions.sanitized(user);
                if (current.path("id").asText().equals(user.path("id").asText()))
                    safeUser.set("effectivePermissions", effectivePermissions.deepCopy());
                users.add(safeUser);
            }
        }
        out.set("users", users);

        ArrayNode subs = data.array();
        if (access.platform() || access.manager()) for (JsonNode item : data.all("subscriptions"))
            if (access.platform() || org.equals(item.path("organizationId").asText())) subs.add(item.deepCopy());
        out.set("subscriptions", subs);

        ArrayNode invites = data.array();
        if (access.platform() || access.manager()) for (JsonNode item : data.all("invitations"))
            if (access.platform() || org.equals(item.path("organizationId").asText())) invites.add(item.deepCopy());
        out.set("invitations", invites);

        ArrayNode requests = data.array();
        if (access.platform()) for (JsonNode item : data.all("accountRequests")) requests.add(item.deepCopy());
        out.set("accountRequests", requests);

        for (String[] entry : COLLECTIONS) {
            ArrayNode rows = data.array();
            if (access.allowed(entry[1])) {
                for (JsonNode item : data.all(entry[0]))
                    if (access.canRead(item)) rows.add(item.deepCopy());
            }
            out.set(entry[0], rows);
        }

        // El frontend utiliza permisos efectivos, pero el servidor siempre vuelve a verificar cada escritura.
        ObjectNode overrides = data.object();
        if (access.platform() || access.manager()) for (JsonNode assignment : data.all("permissionAssignments")) {
            if (!access.platform() && !org.equals(assignment.path("organizationId").asText())) continue;
            ObjectNode values = data.object();
            for (JsonNode group : access.catalog().path("groups"))
                for (JsonNode permission : group.path("permissions")) values.put(permission.asText(), false);
            for (JsonNode permission : assignment.path("permissions")) values.put(permission.asText(), true);
            overrides.set(assignment.path("userId").asText(), values);
        }
        out.set("permissionOverrides", overrides);
        return out;
    }
}
