package com.zempay.audit.service;

import com.zempay.audit.dto.AuditLogResponse;
import com.zempay.audit.entity.AuditLog;
import com.zempay.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(
            String actorType,
            UUID actorId,
            String action,
            String entityType,
            UUID entityId,
            Map<String, Object> details) {

        AuditLog auditLog = new AuditLog(
                actorType,
                actorId,
                action,
                entityType,
                entityId,
                details
        );

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> search(
            UUID actorId,
            String action,
            String entityType,
            UUID entityId,
            Pageable pageable) {

        return auditLogRepository.searchAuditLogs(
                actorId,
                action,
                entityType,
                entityId,
                pageable
        );
    }

    public AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorType(),
                auditLog.getActorId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }
}