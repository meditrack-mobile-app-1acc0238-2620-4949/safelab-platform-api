package com.safelab.platform.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas sin JSON Server: la documentación y el filtro no dependen del seed. */
@SpringBootTest
@AutoConfigureMockMvc
class BilingualDocumentationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void bothLanguagesDocumentExactlyTheSameEndpoints() throws Exception {
        JsonNode es = spec("es");
        JsonNode en = spec("en");
        assertEquals(keys(es.path("paths")), keys(en.path("paths")));
        assertEquals(66, countOperations(es.path("paths")));
        assertEquals(66, countOperations(en.path("paths")));
        assertEquals("Iniciar sesión con correo o username",
            es.path("paths").path("/api/v1/auth/login").path("post").path("summary").asText());
        assertEquals("Sign in with email or username",
            en.path("paths").path("/api/v1/auth/login").path("post").path("summary").asText());
        assertEquals("01. Gestión de Identidad y Accesos",
            es.path("paths").path("/api/v1/auth/login").path("post").path("tags").get(0).asText());
        assertEquals("01. Identity & Access Management",
            en.path("paths").path("/api/v1/auth/login").path("post").path("tags").get(0).asText());
        assertTrue(es.path("paths").path("/api/v1/platform/users").path("get").path("responses").has("403"));
        assertTrue(en.path("components").path("schemas").has("ApiError"));
    }

    @Test
    void swaggerOffersOnlyTwoLanguageDefinitions() throws Exception {
        JsonNode config = json.readTree(mvc.perform(get("/v3/api-docs/swagger-config"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode urls = config.path("urls");
        assertTrue(urls.isArray(), "Swagger must publish grouped definitions");
        assertEquals(2, urls.size(), "Do not duplicate manually configured language URLs");
        Set<String> names = new HashSet<>();
        for (JsonNode entry : urls) names.add(entry.path("name").asText());
        assertEquals(Set.of("es", "en"), names);
    }

    @Test
    void tagsHaveTheSameStableOrderInBothLanguages() throws Exception {
        JsonNode esTags = spec("es").path("tags");
        JsonNode enTags = spec("en").path("tags");
        assertTrue(esTags.isArray() && enTags.isArray());
        assertEquals(14, esTags.size());
        assertEquals(14, enTags.size());
        assertEquals("00. Salud local", esTags.get(0).path("name").asText());
        assertEquals("00. Local health", enTags.get(0).path("name").asText());
        assertEquals("01. Gestión de Identidad y Accesos", esTags.get(1).path("name").asText());
        assertEquals("01. Identity & Access Management", enTags.get(1).path("name").asText());
        assertEquals("12. Control Remoto y Actuación", esTags.get(13).path("name").asText());
        assertEquals("12. Remote Control & Actuation", enTags.get(13).path("name").asText());
    }

    @Test
    void errorMessageFollowsAcceptLanguageAndKeepsTheErrorCode() throws Exception {
        JsonNode en = unauthorized("en");
        JsonNode es = unauthorized("es");
        assertEquals(401, en.path("status").asInt());
        assertEquals("AUTH_REQUIRED", en.path("code").asText());
        assertEquals("Send Authorization: Bearer <token>.", en.path("message").asText());
        assertEquals("Envía Authorization: Bearer <token>.", es.path("message").asText());
    }

    private JsonNode spec(String locale) throws Exception {
        return json.readTree(mvc.perform(get("/v3/api-docs/" + locale))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private JsonNode unauthorized(String locale) throws Exception {
        return json.readTree(mvc.perform(get("/api/v1/platform/users").header("Accept-Language", locale))
            .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString());
    }
    private Set<String> keys(JsonNode node) {
        Set<String> result = new HashSet<>();
        node.fieldNames().forEachRemaining(result::add);
        return result;
    }
    private int countOperations(JsonNode paths) {
        int count = 0;
        for (JsonNode item : paths) {
            for (Iterator<String> names = item.fieldNames(); names.hasNext();) {
                if (Set.of("get", "post", "put", "patch", "delete").contains(names.next())) count++;
            }
        }
        return count;
    }
}
