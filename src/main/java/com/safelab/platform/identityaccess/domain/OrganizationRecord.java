package com.safelab.platform.identityaccess.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.safelab.platform.shared.domain.StoredEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** Entidad de persistencia PostgreSQL para organizations. */
@Entity(name = "OrganizationRecord")
@Table(name = "organizations")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationRecord extends StoredEntity {
    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "ruc", columnDefinition = "text")
    public String ruc;

    @Column(name = "segment", columnDefinition = "text")
    public String segment;

    @Column(name = "corporate_email", columnDefinition = "text")
    public String corporateEmail;

    @Column(name = "city", columnDefinition = "text")
    public String city;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "manager_id", columnDefinition = "text")
    public String managerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facility_ids", columnDefinition = "jsonb")
    public List<String> facilityIds;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

}
