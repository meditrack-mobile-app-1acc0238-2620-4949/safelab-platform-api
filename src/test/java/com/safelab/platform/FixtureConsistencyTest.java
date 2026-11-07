package com.safelab.platform;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/** Test rapido SIN PostgreSQL: valida la migracion del fixture a SQL. */
class FixtureConsistencyTest {
    private String resource(String path) throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Recurso faltante: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private long count(String sql, String table) {
        return Pattern.compile("(?m)^INSERT INTO " + table + " ").matcher(sql).results().count();
    }

    @Test
    void demoDataCanBeImportedSeparatelyWithHashedPasswords() throws Exception {
        String migration = java.nio.file.Files.readString(java.nio.file.Path.of("database/seed/demo_data.sql"));
        assertEquals(3, count(migration, "organizations"));
        assertEquals(19, count(migration, "users"));
        assertEquals(4, count(migration, "facilities"));
        assertEquals(12, count(migration, "sensors"));
        assertEquals(10, count(migration, "assets"));
        assertEquals(3, count(migration, "subscriptions"));
        assertFalse(migration.contains("SafeLab2026%"), "Nunca guardar la clave demo en texto plano");
        assertTrue(migration.contains("$2a$10$"), "Las claves iniciales deben usar BCrypt");
    }

    @Test
    void schemaUsesSeparateRelationalTablesAndSessions() throws Exception {
        String schema = resource("db/migration/V1__create_safelab_schema.sql");
        assertTrue(schema.contains("CREATE TABLE organizations"));
        assertTrue(schema.contains("CREATE TABLE users"));
        assertTrue(schema.contains("CREATE TABLE sensors"));
        assertTrue(schema.contains("CREATE TABLE subscriptions"));
        assertTrue(schema.contains("CREATE TABLE auth_sessions"));
        assertTrue(schema.contains("FOREIGN KEY (organization_id) REFERENCES organizations(id)"));
    }
}
