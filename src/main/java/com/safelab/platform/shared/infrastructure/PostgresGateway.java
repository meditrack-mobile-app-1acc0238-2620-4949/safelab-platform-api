package com.safelab.platform.shared.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.StoredEntity;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Adaptador PostgreSQL/JPA, en sustitucion completa del servidor JSON.
 * Los controladores conservan sus contratos JsonNode para facilitar la v2.5.
 * Cada coleccion corresponde a una entidad y a una tabla relacional REAL.
 */
@Repository
@org.springframework.context.annotation.Profile("postgres")
public class PostgresGateway implements com.safelab.platform.shared.domain.DataGateway {
    private final EntityManager entityManager;
    private final EntityRegistry registry;
    private final ObjectMapper mapper;

    public PostgresGateway(EntityManager entityManager, EntityRegistry registry, ObjectMapper mapper) {
        this.entityManager = entityManager;
        this.registry = registry;
        this.mapper = mapper;
    }

    public ObjectNode object() { return mapper.createObjectNode(); }
    public ArrayNode array() { return mapper.createArrayNode(); }

    @Transactional(readOnly = true)
    public ArrayNode all(String collection) {
        applyTenantScope();
        var type = registry.type(collection);
        var entities = entityManager.createQuery(
            "SELECT e FROM " + type.getSimpleName() + " e ORDER BY e.id", type
        ).getResultList();
        ArrayNode output = array();
        entities.forEach(entity -> output.add(toJson(entity)));
        return output;
    }

    @Transactional(readOnly = true)
    public ObjectNode get(String collection, String id) {
        applyTenantScope();
        StoredEntity entity = entityManager.find(registry.type(collection), id);
        if (entity == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Registro no encontrado.");
        }
        return toJson(entity);
    }

    @Transactional
    public ObjectNode create(String collection, ObjectNode input) {
        applyTenantScope();
        var type = registry.type(collection);
        if (!input.hasNonNull("id") || input.path("id").asText().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IDENTIFIER", "El registro debe tener un id.");
        }
        String id = input.path("id").asText();
        if (entityManager.find(type, id) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_RECORD", "Ya existe un registro con ese id.");
        }
        StoredEntity entity = fromJson(input, type);
        entityManager.persist(entity);
        entityManager.flush(); // mostrar inmediatamente errores de FK, unicidad, etc.
        return toJson(entity);
    }

    @Transactional
    public ObjectNode patch(String collection, String id, ObjectNode input) {
        applyTenantScope();
        StoredEntity current = entityManager.find(registry.type(collection), id);
        if (current == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Registro no encontrado.");
        }
        ObjectNode merged = toJson(current);
        merged.setAll(input); // PATCH parcial, mismos campos que la API v1.
        // La API de negocio impide modificar el id y la organización.
        merged.put("id", current.id);
        if (current.organizationId != null) merged.put("organizationId", current.organizationId);
        else merged.putNull("organizationId");
        StoredEntity replacement = fromJson(merged, registry.type(collection));
        StoredEntity updated = entityManager.merge(replacement);
        entityManager.flush();
        return toJson(updated);
    }

    @Transactional
    public void delete(String collection, String id) {
        applyTenantScope();
        StoredEntity entity = entityManager.find(registry.type(collection), id);
        if (entity == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Registro no encontrado.");
        }
        entityManager.remove(entity);
        entityManager.flush();
    }


    /**
     * Contexto RLS por TRANSACCION: nunca se conserva entre conexiones Hikari.
     * El filtro de sesion valida el token ANTES de proporcionar la identidad.
     * Para login y registros publicos, el backend necesita leer cuentas globales.
     * Esta variable no protege si se filtran las credenciales safelab_app:
     * la cuenta SQL es exclusivamente tecnica, no se entrega a clientes.
     */
    private void applyTenantScope() {
        JsonNode current = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();
            current = (JsonNode) request.getAttribute("safelab.user");
        }
        boolean platformAccess = current == null || "platformAdmin".equals(current.path("role").asText());
        String organizationId = current == null ? "" : current.path("organizationId").asText("");
        entityManager.createNativeQuery("SELECT set_config('safelab.tenant_id', :org, true)")
            .setParameter("org", organizationId).getSingleResult();
        entityManager.createNativeQuery("SELECT set_config('safelab.platform_access', :allowed, true)")
            .setParameter("allowed", platformAccess ? "true" : "false").getSingleResult();
    }

    private StoredEntity fromJson(ObjectNode source, Class<? extends StoredEntity> type) {
        StoredEntity entity = mapper.convertValue(source, type);
        Set<String> known = Arrays.stream(type.getFields()).map(Field::getName).collect(Collectors.toSet());
        Map<String, Object> extras = new LinkedHashMap<>();
        source.fields().forEachRemaining(entry -> {
            if (!known.contains(entry.getKey())) {
                extras.put(entry.getKey(), mapper.convertValue(entry.getValue(), Object.class));
            }
        });
        entity.extraAttributes = extras;
        return entity;
    }

    private ObjectNode toJson(StoredEntity entity) {
        ObjectNode output = mapper.valueToTree(entity);
        if (entity.extraAttributes != null) {
            entity.extraAttributes.forEach((key, value) -> output.set(key, mapper.valueToTree(value)));
        }
        // El usuario de plataforma no pertenece a ninguna empresa.
        if (entity instanceof com.safelab.platform.identityaccess.domain.UserRecord
                && entity.organizationId == null) output.putNull("organizationId");
        return output;
    }
}
