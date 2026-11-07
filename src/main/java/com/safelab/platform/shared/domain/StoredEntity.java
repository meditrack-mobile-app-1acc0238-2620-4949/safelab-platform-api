package com.safelab.platform.shared.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.LinkedHashMap;
import java.util.Map;

/** Campos compartidos por las entidades persistidas de los Bounded Contexts.
 *  organizationId es nulo solo para la organización y el administrador global.
 *  extraAttributes conserva propiedades nuevas sin modificar el contrato JSON.
 */
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class StoredEntity {
    @Id
    @Column(name = "id", columnDefinition = "text", nullable = false)
    public String id;

    @Column(name = "organization_id", columnDefinition = "text")
    public String organizationId;

    @JsonIgnore
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_attributes", columnDefinition = "jsonb", nullable = false)
    public Map<String, Object> extraAttributes = new LinkedHashMap<>();
}
