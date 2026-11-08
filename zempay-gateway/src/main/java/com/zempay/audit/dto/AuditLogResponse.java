package com.zempay.audit.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String actorType,
        UUID actorId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> details,
        Instant createdAt
) {
}