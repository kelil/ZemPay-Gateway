package com.zempay.merchant.dto;

import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.entity.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String merchantCode,
        String businessName,
        String displayName,
        String email,
        String phone,
        MerchantStatus status,
        Instant createdAt
) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantCode(),
                merchant.getBusinessName(),
                merchant.getDisplayName(),
                merchant.getEmail(),
                merchant.getPhone(),
                merchant.getStatus(),
                merchant.getCreatedAt()
        );
    }
}