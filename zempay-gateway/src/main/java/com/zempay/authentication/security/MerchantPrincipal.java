package com.zempay.authentication.security;

import com.zempay.authentication.entity.MerchantApiKeyEnvironment;

import java.util.UUID;

public record MerchantPrincipal(
        UUID merchantId,
        String merchantCode,
        String displayName,
        MerchantApiKeyEnvironment environment
) {
}