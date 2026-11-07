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

/** Entidad de persistencia PostgreSQL para notifications. */
@Entity(name = "NotificationRecord")
@Table(name = "notifications")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRecord extends StoredEntity {
    @Column(name = "title", columnDefinition = "text")
    public String title;

    @Column(name = "message", columnDefinition = "text")
    public String message;

    @Column(name = "context", columnDefinition = "text")
    public String context;

    @Column(name = "severity", columnDefinition = "text")
    public String severity;

    @Column(name = "unread", columnDefinition = "boolean")
    public Boolean unread;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

}
