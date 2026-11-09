package com.zempay.provider.entity;

import com.zempay.merchant.entity.Merchant;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "merchant_provider_configs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_merchant_provider_environment",
                        columnNames = {"merchant_id", "provider_id", "environment"}
                )
        }
)
public class MerchantProviderConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderEnvironment environment;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private Integer priority = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "credential_data", columnDefinition = "jsonb")
    private Map<String, Object> credentialData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MerchantProviderConfig() {
    }

    public MerchantProviderConfig(
            Merchant merchant,
            PaymentProvider provider,
            ProviderEnvironment environment,
            Integer priority
    ) {
        this.merchant = merchant;
        this.provider = provider;
        this.environment = environment;
        this.priority = priority;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public ProviderEnvironment getEnvironment() {
        return environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public Map<String, Object> getCredentialData() {
        return credentialData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
