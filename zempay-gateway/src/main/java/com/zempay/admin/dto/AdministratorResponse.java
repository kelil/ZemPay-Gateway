package com.zempay.admin.dto;

import com.zempay.admin.entity.AdministratorStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdministratorResponse(
        UUID id,
        String fullName,
        String email,
        AdministratorStatus status,
        Set<String> roles,
        Instant createdAt,
        Instant lastLoginAt
) {
}