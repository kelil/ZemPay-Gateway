package com.zempay.payment.entity;

import com.zempay.provider.entity.PaymentProvider;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "provider_reference", length = 150)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentAttemptStatus status = PaymentAttemptStatus.CREATED;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "initiated_at", nullable = false)
    private Instant initiatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PaymentAttempt() {
    }

    public PaymentAttempt(
            Payment payment,
            PaymentProvider provider,
            Integer attemptNumber
    ) {
        this.payment = payment;
        this.provider = provider;
        this.attemptNumber = attemptNumber;
    }
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (initiatedAt == null) {
            initiatedAt = now;
        }

        createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public PaymentAttemptStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getInitiatedAt() {
        return initiatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void transitionTo(PaymentAttemptStatus newStatus) {

        boolean allowed = switch (this.status) {
            case CREATED ->
                    newStatus == PaymentAttemptStatus.SUBMITTED
                            || newStatus == PaymentAttemptStatus.CANCELLED
                            || newStatus == PaymentAttemptStatus.FAILED;

            case SUBMITTED ->
                    newStatus == PaymentAttemptStatus.PROCESSING
                            || newStatus == PaymentAttemptStatus.PENDING
                            || newStatus == PaymentAttemptStatus.UNKNOWN
                            || newStatus == PaymentAttemptStatus.SUCCEEDED
                            || newStatus == PaymentAttemptStatus.FAILED
                            || newStatus == PaymentAttemptStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == PaymentAttemptStatus.PENDING
                            || newStatus == PaymentAttemptStatus.UNKNOWN
                            || newStatus == PaymentAttemptStatus.SUCCEEDED
                            || newStatus == PaymentAttemptStatus.FAILED
                            || newStatus == PaymentAttemptStatus.CANCELLED;

            case PENDING ->
                    newStatus == PaymentAttemptStatus.PROCESSING
                            || newStatus == PaymentAttemptStatus.UNKNOWN
                            || newStatus == PaymentAttemptStatus.SUCCEEDED
                            || newStatus == PaymentAttemptStatus.FAILED
                            || newStatus == PaymentAttemptStatus.CANCELLED;

            case UNKNOWN ->
                    newStatus == PaymentAttemptStatus.PROCESSING
                            || newStatus == PaymentAttemptStatus.PENDING
                            || newStatus == PaymentAttemptStatus.SUCCEEDED
                            || newStatus == PaymentAttemptStatus.FAILED
                            || newStatus == PaymentAttemptStatus.CANCELLED;

            case SUCCEEDED, FAILED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Payment attempt cannot transition from "
                            + this.status + " to " + newStatus
            );
        }

        this.status = newStatus;

        if (newStatus == PaymentAttemptStatus.SUCCEEDED
                || newStatus == PaymentAttemptStatus.FAILED
                || newStatus == PaymentAttemptStatus.CANCELLED) {
            this.completedAt = Instant.now();
        }
    }
}