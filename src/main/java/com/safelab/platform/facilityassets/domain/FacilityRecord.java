package com.safelab.platform.facilityassets.domain;

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

/** Entidad de persistencia PostgreSQL para facilities. */
@Entity(name = "FacilityRecord")
@Table(name = "facilities")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacilityRecord extends StoredEntity {
    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "city", columnDefinition = "text")
    public String city;

    @Column(name = "manager", columnDefinition = "text")
    public String manager;

    @Column(name = "health_score", columnDefinition = "integer")
    public Integer healthScore;

    @Column(name = "compliance_score", columnDefinition = "integer")
    public Integer complianceScore;

    @Column(name = "telemetry_score", columnDefinition = "integer")
    public Integer telemetryScore;

}
