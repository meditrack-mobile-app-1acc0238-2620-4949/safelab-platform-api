package com.safelab.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class FixtureConsistencyTest {
    @Test void sharedSeedKeepsOrganizationLinks() throws Exception {
        var root=new ObjectMapper().readTree(Files.readString(Path.of("data/db.json")));
        assertEquals(3,root.path("organizations").size());
        assertEquals(19,root.path("users").size());
        assertEquals(12,root.path("sensors").size());
        assertTrue(root.path("permissionAssignments").isArray());
        var ids=new HashSet<String>();
        root.path("organizations").forEach(org->ids.add(org.path("id").asText()));
        for(var collection:new String[]{"users","sensors","assets","incidents","subscriptions"}){
            for(var item:root.path(collection)){
                if(item.hasNonNull("organizationId"))assertTrue(ids.contains(item.path("organizationId").asText()));
            }
        }
    }
}
