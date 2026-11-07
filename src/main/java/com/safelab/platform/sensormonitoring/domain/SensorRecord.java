package com.safelab.platform.sensormonitoring.domain;

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

/** Entidad de persistencia PostgreSQL para sensors. */
@Entity(name = "SensorRecord")
@Table(name = "sensors")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorRecord extends StoredEntity {
    @Column(name = "code", columnDefinition = "text")
    public String code;

    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @Column(name = "asset_id", columnDefinition = "text")
    public String assetId;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "unit", columnDefinition = "text")
    public String unit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb")
    public JsonNode value;

    @Column(name = "min", columnDefinition = "integer")
    public Integer min;

    @Column(name = "max", columnDefinition = "integer")
    public Integer max;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "connection", columnDefinition = "text")
    public String connection;

    @Column(name = "last_reading", columnDefinition = "text")
    public String lastReading;

    @Column(name = "responsible", columnDefinition = "text")
    public String responsible;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
