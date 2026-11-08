package com.zempay.authentication.dto;

import com.zempay.authentication.entity.MerchantApiKeyEnvironment;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MerchantApiKeyCreateRequest(
        @NotNull MerchantApiKeyEnvironment environment,
        Instant expiresAt
) {
}