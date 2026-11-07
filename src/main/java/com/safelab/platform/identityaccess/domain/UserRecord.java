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

/** Entidad de persistencia PostgreSQL para users. */
@Entity(name = "UserRecord")
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRecord extends StoredEntity {
    @Column(name = "username", columnDefinition = "text")
    public String username;

    @Column(name = "email", columnDefinition = "text")
    public String email;

    @Column(name = "password", columnDefinition = "text")
    public String password;

    @Column(name = "first_name", columnDefinition = "text")
    public String firstName;

    @Column(name = "last_name", columnDefinition = "text")
    public String lastName;

    @Column(name = "full_name", columnDefinition = "text")
    public String fullName;

    @Column(name = "initials", columnDefinition = "text")
    public String initials;

    @Column(name = "phone", columnDefinition = "text")
    public String phone;

    @Column(name = "role", columnDefinition = "text")
    public String role;

    @Column(name = "position", columnDefinition = "text")
    public String position;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "organization", columnDefinition = "text")
    public String organization;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facility_ids", columnDefinition = "jsonb")
    public List<String> facilityIds;

    @Column(name = "segment", columnDefinition = "text")
    public String segment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_ids", columnDefinition = "jsonb")
    public List<String> resourceIds;

    @Column(name = "age", columnDefinition = "text")
    public String age;

    @Column(name = "city", columnDefinition = "text")
    public String city;

    @Column(name = "job_title", columnDefinition = "text")
    public String jobTitle;

    @Column(name = "timezone", columnDefinition = "text")
    public String timezone;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

}
