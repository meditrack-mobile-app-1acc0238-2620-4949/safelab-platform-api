package com.safelab.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.safelab.platform.shared.infrastructure.PostgresGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas reales contra safelab_db; cada escritura se revierte al terminar. */
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "SAFELAB_TEST_POSTGRES", matches = "true")
@org.springframework.test.context.ActiveProfiles("postgres")
@SpringBootTest
@Transactional
class PostgresPersistenceTest {
    @Autowired PostgresGateway db;
    @Autowired ObjectMapper json;
    @Autowired PasswordEncoder passwords;

    @Test
    void importsTheOriginalMultiTenantDataAndHashesPasswords() {
        assertEquals(3, db.all("organizations").size());
        assertEquals(19, db.all("users").size());
        assertEquals(12, db.all("sensors").size());
        var admin = db.get("users", "9f63a2e0-6839-4bff-9664-23eb8acfb9ca");
        assertTrue(passwords.matches("SafeLab2026%", admin.path("password").asText()));
        assertNotEquals("SafeLab2026%", admin.path("password").asText());
    }

    @Test
    void patchAndCreateAreStoredInPostgresDuringTheTransaction() {
        var patch = json.createObjectNode();
        patch.put("status", "testing");
        db.patch("sensors", "sen-001", patch);
        assertEquals("testing", db.get("sensors", "sen-001").path("status").asText());

        ObjectNode notification = json.createObjectNode();
        notification.put("id", "notification-test-rollback");
        notification.put("organizationId", "org-andes");
        notification.put("title", "Test de persistencia");
        db.create("notifications", notification);
        assertEquals("org-andes", db.get("notifications", "notification-test-rollback")
            .path("organizationId").asText());
        db.delete("notifications", "notification-test-rollback");
        // Spring Test revierte cualquier cambio en la DB al finalizar.
    }
}
