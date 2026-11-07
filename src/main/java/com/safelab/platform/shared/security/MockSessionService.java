package com.safelab.platform.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.infrastructure.JsonServerGateway;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones efímeras SOLO para practicar API v1; NO equivalen a seguridad de producción. */
@Service
@org.springframework.context.annotation.Profile("jsonserver")
public class MockSessionService implements AuthService {
    public record Session(String userId, Instant expiresAt) { }
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final JsonServerGateway data;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();
    public MockSessionService(JsonServerGateway data, PasswordEncoder encoder) { this.data = data; this.encoder = encoder; }

    private boolean matchesPassword(String raw, String stored) {
        return stored.startsWith("$2") ? encoder.matches(raw, stored) : raw.equals(stored);
    }
    public ObjectNode authenticate(String identifier, String password) {
        JsonNode user = null;
        for (JsonNode u : data.all("users")) {
            if ((identifier.equalsIgnoreCase(u.path("email").asText()) || identifier.equals(u.path("username").asText()))
                && matchesPassword(password, u.path("password").asText())) { user = u; break; }
        }
        if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Correo, usuario o contraseña incorrectos.");
        if (!"active".equalsIgnoreCase(user.path("status").asText()))
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE", "La cuenta aún no está activa.");
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        sessions.put(token, new Session(user.path("id").asText(), expiresAt));
        ObjectNode response = data.object();
        response.put("accessToken", token); response.put("tokenType", "Bearer"); response.put("expiresAt", expiresAt.toString());
        response.set("user", sanitized(user));
        return response;
    }
    public JsonNode byToken(String token) {
        Session session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token); throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Sesión inexistente o expirada. Inicia sesión nuevamente.");
        }
        for (JsonNode u : data.all("users")) if (session.userId().equals(u.path("id").asText())) return u;
        throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "La cuenta de esta sesión ya no existe.");
    }
    public JsonNode current() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        JsonNode user = (JsonNode) request.getAttribute("safelab.user");
        if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Inicia sesión para continuar.");
        return user;
    }
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) sessions.remove(authorization.substring(7));
    }
    public ObjectNode sanitized(JsonNode u) {
        ObjectNode safe = ((ObjectNode)u).deepCopy();
        safe.remove("password");
        return safe;
    }
}
