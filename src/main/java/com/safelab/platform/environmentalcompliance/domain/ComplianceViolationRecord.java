package com.safelab.platform.environmentalcompliance.domain;

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

/** Entidad de persistencia PostgreSQL para complianceViolations. */
@Entity(name = "ComplianceViolationRecord")
@Table(name = "compliance_violations")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceViolationRecord extends StoredEntity {
    @Column(name = "evaluation_id", columnDefinition = "text")
    public String evaluationId;

    @Column(name = "incident_code", columnDefinition = "text")
    public String incidentCode;

    @Column(name = "started_at", columnDefinition = "text")
    public String startedAt;

    @Column(name = "ended_at", columnDefinition = "text")
    public String endedAt;

    @Column(name = "severity", columnDefinition = "text")
    public String severity;

    @Column(name = "status", columnDefinition = "text")
    public String status;

}
