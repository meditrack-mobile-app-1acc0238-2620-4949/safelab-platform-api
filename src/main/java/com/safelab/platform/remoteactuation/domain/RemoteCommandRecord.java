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

/** Entidad de persistencia PostgreSQL para remoteCommands. */
@Entity(name = "RemoteCommandRecord")
@Table(name = "remote_commands")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemoteCommandRecord extends StoredEntity {
    @Column(name = "actuator_id", columnDefinition = "text")
    public String actuatorId;

    @Column(name = "command", columnDefinition = "text")
    public String command;

    @Column(name = "device", columnDefinition = "text")
    public String device;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "requested_by", columnDefinition = "text")
    public String requestedBy;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

}
