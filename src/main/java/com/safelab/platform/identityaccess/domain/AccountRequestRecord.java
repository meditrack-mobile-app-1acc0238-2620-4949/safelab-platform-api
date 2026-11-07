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

/** Entidad de persistencia PostgreSQL para accountRequests. */
@Entity(name = "AccountRequestRecord")
@Table(name = "account_requests")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountRequestRecord extends StoredEntity {
    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "email", columnDefinition = "text")
    public String email;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

}
