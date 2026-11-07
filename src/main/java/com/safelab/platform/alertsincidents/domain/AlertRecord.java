package com.safelab.platform.alertsincidents.domain;

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

/** Entidad de persistencia PostgreSQL para alerts. */
@Entity(name = "AlertRecord")
@Table(name = "alerts")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertRecord extends StoredEntity {
    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "title", columnDefinition = "text")
    public String title;

    @Column(name = "message", columnDefinition = "text")
    public String message;

    @Column(name = "sensor_id", columnDefinition = "text")
    public String sensorId;

    @Column(name = "asset_id", columnDefinition = "text")
    public String assetId;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @Column(name = "severity", columnDefinition = "text")
    public String severity;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "assigned_to", columnDefinition = "text")
    public String assignedTo;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

}
