package com.safelab.platform.shared.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import com.safelab.platform.shared.security.AccessRules;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

/** CRUD reutilizable por BC con filtros multiempresa. */
@Service
public class RecordsService {
    private final JsonServerGateway db;
    private final AccessRules access;
    public RecordsService(JsonServerGateway db, AccessRules access) { this.db=db; this.access=access; }
    public ArrayNode list(String collection, String permission) {
        access.require(permission);
        ArrayNode result=db.array();
        for (JsonNode item : db.all(collection)) if (access.canRead(item)) result.add(safe(item));
        return result;
    }
    public ObjectNode get(String collection,String id,String permission) {
        access.require(permission);
        JsonNode item=db.get(collection,id); access.accessItem(item); return safe(item);
    }
    public ObjectNode create(String collection, ObjectNode input, String permission) {
        access.require(permission);
        String owner = access.platform() ? input.path("organizationId").asText("") : access.org();
        if (owner.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST,"ORGANIZATION_REQUIRED","Debes indicar la empresa del registro.");
        access.sameOrg(owner);
        if (!existsOrg(owner)) throw new ApiException(HttpStatus.NOT_FOUND,"ORGANIZATION_NOT_FOUND","La empresa no existe.");
        ObjectNode body=input.deepCopy();
        body.put("organizationId",owner);
        validateReferences(body,owner);
        if (!body.hasNonNull("id")) body.put("id",collection+"-"+UUID.randomUUID());
        if (!body.has("createdAt")) body.put("createdAt",Instant.now().toString());
        ObjectNode created = db.create(collection,body);
        audit("created",collection,created.path("id").asText(),owner);
        return safe(created);
    }
    public ObjectNode patch(String collection,String id,ObjectNode input,String permission) {
        access.require(permission); JsonNode stored=db.get(collection,id);access.accessItem(stored);
        if (input.has("id") || input.has("organizationId"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"IMMUTABLE_FIELDS","No puedes modificar id ni organizationId.");
        ObjectNode patch=input.deepCopy(); validateReferences(patch,stored.path("organizationId").asText()); patch.put("updatedAt",Instant.now().toString());
        ObjectNode updated = db.patch(collection,id,patch);
        audit("updated",collection,id,stored.path("organizationId").asText());
        return safe(updated);
    }
    public void delete(String collection,String id,String permission) {
        access.require(permission); JsonNode stored=db.get(collection,id);access.accessItem(stored);
        db.delete(collection,id);
        audit("deleted",collection,id,stored.path("organizationId").asText());
    }
    public ObjectNode safe(JsonNode item) {
        ObjectNode copy=((ObjectNode)item).deepCopy(); copy.remove("password");return copy;
    }
    private void audit(String action,String collection,String recordId,String organizationId){
        ObjectNode event=db.object();event.put("id","aud-"+UUID.randomUUID());
        event.put("organizationId",organizationId);event.put("actor",access.user().path("fullName").asText());
        event.put("action",action+" "+collection+"/"+recordId);event.put("module",collection);
        event.put("severity","info");event.put("status","reviewed");
        event.put("createdAt",Instant.now().toString());
        db.create("auditLogs",event);
    }
    private void validateReferences(ObjectNode body,String org){
        String[][] refs={{"facilityId","facilities"},{"assetId","assets"},{"equipmentId","assets"},
            {"sensorId","sensors"},{"relatedSensorId","sensors"},{"linkedSensorId","sensors"},
            {"actuatorId","actuators"},{"ruleId","complianceRules"}};
        for(String[] ref:refs){
            if(body.hasNonNull(ref[0])&&!body.path(ref[0]).asText().isBlank()){
                JsonNode linked=db.get(ref[1],body.path(ref[0]).asText());
                if(!org.equals(linked.path("organizationId").asText()))access.denied();
            }
        }
    }
    public boolean existsOrg(String org) {
        for(JsonNode item:db.all("organizations")) if(org.equals(item.path("id").asText()))return true;
        return false;
    }
}
