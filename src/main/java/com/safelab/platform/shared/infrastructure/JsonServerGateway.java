package com.safelab.platform.shared.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.regex.Pattern;

/** Único punto de conexión a JSON Server. En v2 se sustituye por un adaptador PostgreSQL. */
@Component
public class JsonServerGateway {
    private static final Pattern SAFE_PATH = Pattern.compile("[a-zA-Z0-9_-]+");
    private final RestClient http;
    private final ObjectMapper mapper;

    public JsonServerGateway(@Value("${safelab.json-server-url}") String url, ObjectMapper mapper) {
        this.http = RestClient.builder().baseUrl(url).build();
        this.mapper = mapper;
    }
    public ObjectNode object() { return mapper.createObjectNode(); }
    public ArrayNode array() { return mapper.createArrayNode(); }

    public JsonNode get(String collection) { return read(() -> http.get().uri("/" + safe(collection)).retrieve().body(JsonNode.class)); }
    public JsonNode get(String collection, String id) {
        return read(() -> http.get().uri("/" + safe(collection) + "/" + safe(id)).retrieve().body(JsonNode.class));
    }
    public ArrayNode all(String collection) {
        JsonNode value = get(collection);
        if (!value.isArray()) throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "DATA_SHAPE", "La colección no tiene formato de lista: " + collection);
        return (ArrayNode)value;
    }
    public ObjectNode create(String collection, ObjectNode body) {
        return (ObjectNode)read(() -> http.post().uri("/" + safe(collection)).body(body).retrieve().body(JsonNode.class));
    }
    public ObjectNode patch(String collection, String id, ObjectNode patch) {
        return (ObjectNode)read(() -> http.patch().uri("/" + safe(collection) + "/" + safe(id)).body(patch).retrieve().body(JsonNode.class));
    }
    public void delete(String collection, String id) {
        read(() -> { http.delete().uri("/" + safe(collection) + "/" + safe(id)).retrieve().toBodilessEntity(); return null; });
    }
    private String safe(String segment) {
        if (segment == null || !SAFE_PATH.matcher(segment).matches())
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IDENTIFIER", "Identificador no válido.");
        return segment;
    }
    private <T> T read(java.util.function.Supplier<T> action) {
        try { return action.get(); }
        catch (ResourceAccessException e) { throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "DATA_SOURCE_UNAVAILABLE", "JSON Server no está disponible en localhost:3000."); }
        catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Registro no encontrado.");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "DATA_SOURCE_ERROR", "JSON Server devolvió un error HTTP " + e.getStatusCode().value());
        }
    }
}
