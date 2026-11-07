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

/** Entidad de persistencia PostgreSQL para complianceRules. */
@Entity(name = "ComplianceRuleRecord")
@Table(name = "compliance_rules")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceRuleRecord extends StoredEntity {
    @Column(name = "title", columnDefinition = "text")
    public String title;

    @Column(name = "title_es", columnDefinition = "text")
    public String titleEs;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "sensor_id", columnDefinition = "text")
    public String sensorId;

    @Column(name = "min", columnDefinition = "integer")
    public Integer min;

    @Column(name = "max", columnDefinition = "integer")
    public Integer max;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "required_evidence", columnDefinition = "boolean")
    public Boolean requiredEvidence;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
