package com.zempay.payment.dto;

import com.zempay.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String merchantReference,
        BigDecimal amount,
        String currencyCode,
        PaymentStatus status,
        String selectedProviderCode,
        Instant expiresAt,
        Instant createdAt
) {}