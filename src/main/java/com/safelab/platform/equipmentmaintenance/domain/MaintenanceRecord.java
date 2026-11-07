package com.safelab.platform.equipmentmaintenance.domain;

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

/** Entidad de persistencia PostgreSQL para maintenanceRecords. */
@Entity(name = "MaintenanceRecord")
@Table(name = "maintenance_records")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceRecord extends StoredEntity {
    @Column(name = "equipment_id", columnDefinition = "text")
    public String equipmentId;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "date", columnDefinition = "text")
    public String date;

    @Column(name = "responsible", columnDefinition = "text")
    public String responsible;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
