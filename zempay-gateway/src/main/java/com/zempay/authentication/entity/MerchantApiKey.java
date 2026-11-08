package com.zempay.authentication.entity;

import com.zempay.merchant.entity.Merchant;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchant_api_keys")
public class MerchantApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "key_prefix", nullable = false, unique = true, length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, length = 255)
    private String keyHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantApiKeyEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantApiKeyStatus status = MerchantApiKeyStatus.ACTIVE;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MerchantApiKey() {
    }

    public MerchantApiKey(
            Merchant merchant,
            String keyPrefix,
            String keyHash,
            MerchantApiKeyEnvironment environment,
            Instant expiresAt
    ) {
        this.merchant = merchant;
        this.keyPrefix = keyPrefix;
        this.keyHash = keyHash;
        this.environment = environment;
        this.expiresAt = expiresAt;
        this.status = MerchantApiKeyStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void revoke() {
        if (this.status != MerchantApiKeyStatus.ACTIVE) {
            throw new IllegalStateException("Only active API keys can be revoked.");
        }
        this.status = MerchantApiKeyStatus.REVOKED;
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public void markExpired() {
        this.status = MerchantApiKeyStatus.EXPIRED;
    }

    public boolean isExpired() {
        return expiresAt != null && !expiresAt.isAfter(Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public MerchantApiKeyEnvironment getEnvironment() {
        return environment;
    }

    public MerchantApiKeyStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}