package com.safelab.platform.shared.domain;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Abstraccion unica: los 12 BC no conocen la fuente de datos. */
public interface DataGateway {
    ObjectNode object();
    ArrayNode array();
    ArrayNode all(String collection);
    ObjectNode get(String collection, String id);
    ObjectNode create(String collection, ObjectNode input);
    ObjectNode patch(String collection, String id, ObjectNode input);
    void delete(String collection, String id);
}
