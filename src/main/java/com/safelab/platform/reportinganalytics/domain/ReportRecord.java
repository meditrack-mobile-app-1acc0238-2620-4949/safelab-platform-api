package com.safelab.platform.reportinganalytics.domain;

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

/** Entidad de persistencia PostgreSQL para reports. */
@Entity(name = "ReportRecord")
@Table(name = "reports")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportRecord extends StoredEntity {
    @Column(name = "title", columnDefinition = "text")
    public String title;

    @Column(name = "type", columnDefinition = "text")
    public String type;

    @Column(name = "format", columnDefinition = "text")
    public String format;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "generated_at", columnDefinition = "text")
    public String generatedAt;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
