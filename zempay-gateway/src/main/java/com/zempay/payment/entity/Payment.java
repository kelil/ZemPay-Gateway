package com.zempay.payment.entity;

import com.zempay.merchant.entity.Merchant;
import com.zempay.provider.entity.PaymentProvider;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "merchant_reference", nullable = false, length = 100)
    private String merchantReference;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "currency_code",
            nullable = false,
            columnDefinition = "CHAR(3)"
    )
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_provider_id")
    private PaymentProvider selectedProvider;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {
    }

    public Payment(
            Merchant merchant,
            String merchantReference,
            BigDecimal amount,
            String currencyCode,
            String description,
            PaymentProvider selectedProvider,
            String returnUrl,
            Instant expiresAt
    ) {
        this.merchant = merchant;
        this.merchantReference = merchantReference;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.description = description;
        this.selectedProvider = selectedProvider;
        this.returnUrl = returnUrl;
        this.expiresAt = expiresAt;
    }

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

    public UUID getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public PaymentProvider getSelectedProvider() {
        return selectedProvider;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void transitionTo(PaymentStatus newStatus) {

        boolean allowed = switch (this.status) {
            case CREATED ->
                    newStatus == PaymentStatus.PENDING
                            || newStatus == PaymentStatus.CANCELLED
                            || newStatus == PaymentStatus.EXPIRED;

            case PENDING ->
                    newStatus == PaymentStatus.PROCESSING
                            || newStatus == PaymentStatus.REQUIRES_ACTION
                            || newStatus == PaymentStatus.CANCELLED
                            || newStatus == PaymentStatus.EXPIRED;

            case REQUIRES_ACTION ->
                    newStatus == PaymentStatus.PROCESSING
                            || newStatus == PaymentStatus.CANCELLED
                            || newStatus == PaymentStatus.EXPIRED;

            case PROCESSING ->
                    newStatus == PaymentStatus.SUCCEEDED
                            || newStatus == PaymentStatus.FAILED
                            || newStatus == PaymentStatus.PENDING;

            case SUCCEEDED, FAILED, CANCELLED, EXPIRED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Payment cannot transition from "
                            + this.status + " to " + newStatus
            );
        }

        this.status = newStatus;
    }
}