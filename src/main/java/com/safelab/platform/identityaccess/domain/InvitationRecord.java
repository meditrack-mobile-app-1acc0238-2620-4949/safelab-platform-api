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

/** Entidad de persistencia PostgreSQL para invitations. */
@Entity(name = "InvitationRecord")
@Table(name = "invitations")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvitationRecord extends StoredEntity {
    @Column(name = "code", columnDefinition = "text")
    public String code;

    @Column(name = "email", columnDefinition = "text")
    public String email;

    @Column(name = "role", columnDefinition = "text")
    public String role;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "expires_at", columnDefinition = "text")
    public String expiresAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "used_by", columnDefinition = "text")
    public String usedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facility_ids", columnDefinition = "jsonb")
    public List<String> facilityIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_ids", columnDefinition = "jsonb")
    public List<String> resourceIds;

}
