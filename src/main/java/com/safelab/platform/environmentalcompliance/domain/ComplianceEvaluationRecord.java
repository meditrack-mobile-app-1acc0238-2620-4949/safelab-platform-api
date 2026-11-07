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

/** Entidad de persistencia PostgreSQL para complianceEvaluations. */
@Entity(name = "ComplianceEvaluationRecord")
@Table(name = "compliance_evaluations")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplianceEvaluationRecord extends StoredEntity {
    @Column(name = "rule_id", columnDefinition = "text")
    public String ruleId;

    @Column(name = "sensor_id", columnDefinition = "text")
    public String sensorId;

    @Column(name = "observed_at", columnDefinition = "text")
    public String observedAt;

    @Column(name = "value", columnDefinition = "double precision")
    public Double value;

    @Column(name = "status", columnDefinition = "text")
    public String status;

}
