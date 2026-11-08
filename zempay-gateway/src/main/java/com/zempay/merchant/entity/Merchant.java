package com.zempay.merchant.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_code", nullable = false, unique = true, length = 50)
    private String merchantCode;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MerchantStatus status = MerchantStatus.PENDING;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }


    public void approve(UUID administratorId) {
        if (this.status != MerchantStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending merchants can be approved."
            );
        }

        if (administratorId == null) {
            throw new IllegalArgumentException(
                    "Administrator ID is required."
            );
        }

        this.status = MerchantStatus.ACTIVE;
        this.approvedBy = administratorId;
        this.approvedAt = Instant.now();
    }

    public void reject() {
        if (this.status != MerchantStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending merchants can be rejected."
            );
        }

        this.status = MerchantStatus.REJECTED;
    }

    public void suspend() {
        if (this.status != MerchantStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active merchants can be suspended."
            );
        }

        this.status = MerchantStatus.SUSPENDED;
    }
    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}