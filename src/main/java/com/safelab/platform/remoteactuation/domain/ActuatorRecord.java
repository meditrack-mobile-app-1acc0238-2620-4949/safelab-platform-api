package com.safelab.platform.remoteactuation.domain;

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

/** Entidad de persistencia PostgreSQL para actuators. */
@Entity(name = "ActuatorRecord")
@Table(name = "actuators")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActuatorRecord extends StoredEntity {
    @Column(name = "asset_id", columnDefinition = "text")
    public String assetId;

    @Column(name = "code", columnDefinition = "text")
    public String code;

    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @Column(name = "location", columnDefinition = "text")
    public String location;

    @Column(name = "linked_sensor_id", columnDefinition = "text")
    public String linkedSensorId;

    @Column(name = "safety_state", columnDefinition = "text")
    public String safetyState;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "running", columnDefinition = "boolean")
    public Boolean running;

    @Column(name = "last_command", columnDefinition = "text")
    public String lastCommand;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
