package com.safelab.platform.audittraceability.domain;

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

/** Entidad de persistencia PostgreSQL para auditLogs. */
@Entity(name = "AuditLogRecord")
@Table(name = "audit_logs")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogRecord extends StoredEntity {
    @Column(name = "actor", columnDefinition = "text")
    public String actor;

    @Column(name = "action", columnDefinition = "text")
    public String action;

    @Column(name = "module", columnDefinition = "text")
    public String module;

    @Column(name = "severity", columnDefinition = "text")
    public String severity;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "evidence", columnDefinition = "text")
    public String evidence;

}
