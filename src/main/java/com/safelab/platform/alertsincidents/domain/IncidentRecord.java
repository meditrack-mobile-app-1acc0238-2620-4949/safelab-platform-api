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

/** Entidad de persistencia PostgreSQL para incidents. */
@Entity(name = "IncidentRecord")
@Table(name = "incidents")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentRecord extends StoredEntity {
    @Column(name = "code", columnDefinition = "text")
    public String code;

    @Column(name = "title", columnDefinition = "text")
    public String title;

    @Column(name = "description", columnDefinition = "text")
    public String description;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @Column(name = "related_alert_id", columnDefinition = "text")
    public String relatedAlertId;

    @Column(name = "related_sensor_id", columnDefinition = "text")
    public String relatedSensorId;

    @Column(name = "related_asset_id", columnDefinition = "text")
    public String relatedAssetId;

    @Column(name = "severity", columnDefinition = "text")
    public String severity;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "assigned_to", columnDefinition = "text")
    public String assignedTo;

    @Column(name = "due_date", columnDefinition = "text")
    public String dueDate;

    @Column(name = "evidence", columnDefinition = "integer")
    public Integer evidence;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

}
