package com.safelab.platform.shared.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.domain.ApiException;
import com.safelab.platform.shared.domain.DataGateway;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/** Autenticacion local con BCrypt y tokens opacos persistidos en PostgreSQL.
 * No sustituye OAuth2, HTTPS ni la gestion de secretos de la v3.
 */
@Service
@org.springframework.context.annotation.Profile("postgres")
public class SessionService implements AuthService {
    private final EntityManager em;
    private final DataGateway data;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public SessionService(EntityManager em, DataGateway data, PasswordEncoder encoder) {
        this.em = em; this.data = data; this.encoder = encoder;
    }

    @Transactional
    public ObjectNode authenticate(String identifier, String password) {
        JsonNode user = null;
        for (JsonNode candidate : data.all("users")) {
            if (identifier.equalsIgnoreCase(candidate.path("email").asText())
                    || identifier.equals(candidate.path("username").asText())) {
                user = candidate;
                break;
            }
        }
        if (user == null || !encoder.matches(password, user.path("password").asText())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Correo, usuario o contraseña incorrectos.");
        }
        if (!"active".equalsIgnoreCase(user.path("status").asText())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE", "La cuenta aún no está activa.");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        AuthSessionRecord record = new AuthSessionRecord();
        record.tokenHash = sha256(token);
        record.userId = user.path("id").asText();
        record.expiresAt = expiresAt;
        em.persist(record);

        ObjectNode response = data.object();
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("expiresAt", expiresAt.toString());
        response.set("user", sanitized(user));
        return response;
    }

    @Transactional
    public JsonNode byToken(String token) {
        AuthSessionRecord session = em.find(AuthSessionRecord.class, sha256(token));
        if (session == null || !session.expiresAt.isAfter(Instant.now())) {
            if (session != null) em.remove(session);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Sesión inexistente o expirada. Inicia sesión nuevamente.");
        }
        JsonNode user;
        try {
            user = data.get("users", session.userId);
        } catch (ApiException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "La cuenta de esta sesión ya no existe.");
        }
        if (!"active".equalsIgnoreCase(user.path("status").asText())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVE", "La cuenta aún no está activa.");
        }
        return user;
    }

    public JsonNode current() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        JsonNode user = (JsonNode) request.getAttribute("safelab.user");
        if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Inicia sesión para continuar.");
        return user;
    }

    @Transactional
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            AuthSessionRecord session = em.find(AuthSessionRecord.class, sha256(authorization.substring(7)));
            if (session != null) em.remove(session);
        }
    }

    public ObjectNode sanitized(JsonNode value) {
        ObjectNode copy = ((ObjectNode) value).deepCopy();
        copy.remove("password");
        return copy;
    }

    private String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
