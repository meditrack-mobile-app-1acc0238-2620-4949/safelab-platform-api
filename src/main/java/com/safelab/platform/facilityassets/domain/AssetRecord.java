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

/** Entidad de persistencia PostgreSQL para assets. */
@Entity(name = "AssetRecord")
@Table(name = "assets")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetRecord extends StoredEntity {
    @Column(name = "name", columnDefinition = "text")
    public String name;

    @Column(name = "category", columnDefinition = "text")
    public String category;

    @Column(name = "facility_id", columnDefinition = "text")
    public String facilityId;

    @Column(name = "storage_unit", columnDefinition = "text")
    public String storageUnit;

    @Column(name = "location", columnDefinition = "text")
    public String location;

    @Column(name = "last_inspection", columnDefinition = "text")
    public String lastInspection;

    @Column(name = "responsible", columnDefinition = "text")
    public String responsible;

    @Column(name = "responsible_user_id", columnDefinition = "text")
    public String responsibleUserId;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "scope", columnDefinition = "text")
    public String scope;

    @Column(name = "updated_at", columnDefinition = "text")
    public String updatedAt;

    @Column(name = "created_at", columnDefinition = "text")
    public String createdAt;

}
