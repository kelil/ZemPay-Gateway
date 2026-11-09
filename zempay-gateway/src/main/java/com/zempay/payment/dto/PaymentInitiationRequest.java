package com.zempay.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentInitiationRequest(

        @NotBlank
        @Size(max = 100)
        String merchantReference,

        @NotNull
        @DecimalMin(value = "0.00000001")
        @Digits(integer = 12, fraction = 8)
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currencyCode,

        @Size(max = 500)
        String description,

        String selectedProviderCode,

        String returnUrl,

        Instant expiresAt

) {}