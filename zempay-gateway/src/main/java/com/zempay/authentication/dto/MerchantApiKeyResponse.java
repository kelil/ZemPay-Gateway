package com.zempay.authentication.dto;

import com.zempay.authentication.entity.MerchantApiKeyEnvironment;
import com.zempay.authentication.entity.MerchantApiKeyStatus;

import java.time.Instant;
import java.util.UUID;

public record MerchantApiKeyResponse(
        UUID id,
        UUID merchantId,
        String keyPrefix,
        String apiKey,
        MerchantApiKeyEnvironment environment,
        MerchantApiKeyStatus status,
        Instant expiresAt,
        Instant createdAt
) {
}