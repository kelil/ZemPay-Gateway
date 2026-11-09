package com.zempay.payment.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "minor_units", nullable = false)
    private Short minorUnits;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Currency() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Short getMinorUnits() {
        return minorUnits;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}