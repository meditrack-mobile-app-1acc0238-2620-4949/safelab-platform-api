package com.safelab.platform.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Contrato de sesiones, implementado por cada modo local. */
public interface AuthService {
    ObjectNode authenticate(String identifier, String password);
    JsonNode byToken(String token);
    JsonNode current();
    void logout();
    ObjectNode sanitized(JsonNode value);
}
