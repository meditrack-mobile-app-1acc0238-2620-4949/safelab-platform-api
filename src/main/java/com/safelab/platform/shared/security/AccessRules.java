package com.safelab.platform.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/** RBAC y límites de empresa en un solo lugar. No usar parámetros para decidir el usuario. */
@Service
public class AccessRules {
    private final AuthService auth;
    private final DataGateway data;
    private final JsonNode catalog;
    public AccessRules(AuthService auth, DataGateway data, ObjectMapper mapper) throws IOException {
        this.auth = auth; this.data = data;
        try (var input = new ClassPathResource("permission-catalog.json").getInputStream()) { this.catalog = mapper.readTree(input); }
    }
    public JsonNode user() { return auth.current(); }
    public boolean platform() { return "platformAdmin".equals(user().path("role").asText()); }
    public boolean manager() { return "organizationAdmin".equals(user().path("role").asText()); }
    public String org() { return user().path("organizationId").asText(""); }
    public void platformOnly() { if (!platform()) denied(); }
    public void managerOnly() { if (!manager()) denied(); }
    public void sameOrg(String organizationId) {
        if (!platform() && !org().equals(organizationId)) denied();
    }
    public void require(String permission) { if (!allowed(permission)) denied(); }
    public boolean allowed(String permission) {
        if (platform() || manager()) return true;
        String userId = user().path("id").asText();
        for (JsonNode entry : data.all("permissionAssignments")) {
            if (userId.equals(entry.path("userId").asText())) {
                for (JsonNode p : entry.path("permissions")) if (permission.equals(p.asText())) return true;
                return false;
            }
        }
        for (JsonNode p : catalog.path("defaults").path(user().path("role").asText())) if (permission.equals(p.asText())) return true;
        return false;
    }
    public JsonNode catalog() { return catalog; }
    public boolean canRead(JsonNode item) {
        if (platform()) return true;
        if (item.hasNonNull("organizationId") && !org().equals(item.path("organizationId").asText())) return false;
        if (!item.hasNonNull("organizationId")) return false;
        if (manager()) return true;
        JsonNode ids = user().path("resourceIds");
        if (!ids.isArray() || ids.size()==0) return true; // coordinadores con alcance global en su empresa
        String id = item.path("id").asText();
        String asset = item.path("assetId").asText();
        String sensor = item.path("sensorId").asText();
        String equipment = item.path("equipmentId").asText();
        String linked = item.path("linkedSensorId").asText();
        // Si son datos empresariales no ligados a un recurso, permiten consulta por permisos.
        if (asset.isBlank() && sensor.isBlank() && equipment.isBlank() && linked.isBlank() && !isResource(item)) return true;
        for (JsonNode allowed : ids) {
            String rid=allowed.asText();
            if (rid.equals(id) || rid.equals(asset) || rid.equals(sensor) || rid.equals(equipment) || rid.equals(linked)) return true;
        }
        return false;
    }
    private boolean isResource(JsonNode item) {
        return item.has("connection") || item.has("storageUnit") || item.has("category");
    }
    public void accessItem(JsonNode item) { if (!canRead(item)) denied(); }
    public void denied() { throw new ApiException(HttpStatus.FORBIDDEN,"ACCESS_DENIED","No tienes permisos para esta empresa o recurso."); }
}
