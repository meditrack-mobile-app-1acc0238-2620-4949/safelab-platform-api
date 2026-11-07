package com.safelab.platform.subscriptionbilling.domain;

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

/** Entidad de persistencia PostgreSQL para subscriptions. */
@Entity(name = "SubscriptionRecord")
@Table(name = "subscriptions")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionRecord extends StoredEntity {
    @Column(name = "plan", columnDefinition = "text")
    public String plan;

    @Column(name = "status", columnDefinition = "text")
    public String status;

    @Column(name = "start_date", columnDefinition = "text")
    public String startDate;

    @Column(name = "renewal_date", columnDefinition = "text")
    public String renewalDate;

    @Column(name = "included_seats", columnDefinition = "integer")
    public Integer includedSeats;

    @Column(name = "additional_seats", columnDefinition = "integer")
    public Integer additionalSeats;

    @Column(name = "seat_price", columnDefinition = "integer")
    public Integer seatPrice;

    @Column(name = "base_amount", columnDefinition = "integer")
    public Integer baseAmount;

    @Column(name = "monthly_cost", columnDefinition = "integer")
    public Integer monthlyCost;

    @Column(name = "currency_code", columnDefinition = "text")
    public String currencyCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "invoices", columnDefinition = "jsonb")
    public List<Map<String,Object>> invoices;

}
