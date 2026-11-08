package com.zempay.admin.service;

import com.zempay.admin.dto.AdministratorCreateRequest;
import com.zempay.admin.dto.AdministratorResponse;
import com.zempay.admin.entity.Administrator;
import com.zempay.admin.entity.AdministratorRole;
import com.zempay.admin.entity.AdministratorStatus;
import com.zempay.admin.repository.AdministratorRepository;
import com.zempay.admin.repository.AdministratorRoleRepository;
import com.zempay.audit.AuditActions;
import com.zempay.audit.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final AdministratorRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdministratorService(
            AdministratorRepository administratorRepository,
            AdministratorRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.administratorRepository = administratorRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AdministratorResponse createAdministrator(
            AdministratorCreateRequest request) {

        // Used by the initial bootstrap process.
        return createAdministrator(
                request,
                "SYSTEM",
                null,
                AuditActions.ADMINISTRATOR_BOOTSTRAPPED
        );
    }

    @Transactional
    public AdministratorResponse createAdministrator(
            AdministratorCreateRequest request,
            UUID actorId) {

        // Used when an authenticated administrator creates another administrator.
        if (actorId == null) {
            throw new IllegalArgumentException(
                    "Administrator actor ID is required."
            );
        }

        return createAdministrator(
                request,
                "ADMINISTRATOR",
                actorId,
                AuditActions.ADMINISTRATOR_CREATED
        );
    }

    private AdministratorResponse createAdministrator(
            AdministratorCreateRequest request,
            String actorType,
            UUID actorId,
            String action) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (administratorRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An administrator with this email already exists."
            );
        }

        if (request.roles() == null || request.roles().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one administrator role is required."
            );
        }

        Administrator administrator = new Administrator(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password())
        );

        Set<String> roleCodes = request.roles().stream()
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (String roleCode : roleCodes) {
            AdministratorRole role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid administrator role: " + roleCode
                    ));

            administrator.assignRole(role);
        }

        Administrator saved = administratorRepository.save(administrator);

        auditLogService.record(
                actorType,
                actorId,
                action,
                "ADMINISTRATOR",
                saved.getId(),
                Map.of(
                        "email", saved.getEmail(),
                        "roles", roleCodes
                )
        );

        return toResponse(saved);
    }

    private AdministratorResponse toResponse(Administrator administrator) {
        Set<String> roles = administrator.getRoles().stream()
                .map(AdministratorRole::getRoleCode)
                .collect(Collectors.toSet());

        return new AdministratorResponse(
                administrator.getId(),
                administrator.getFullName(),
                administrator.getEmail(),
                administrator.getStatus(),
                roles,
                administrator.getCreatedAt(),
                administrator.getLastLoginAt()
        );
    }

    @Transactional(readOnly = true)
    public Administrator getAuthenticatedAdministrator(String email) {

        return administratorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated administrator not found."
                ));
    }

    @Transactional(readOnly = true)
    public Page<AdministratorResponse> getAdministrators(Pageable pageable) {

        return administratorRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdministratorResponse getAdministratorById(UUID administratorId) {

        Administrator administrator = administratorRepository
                .findById(administratorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Administrator not found."
                ));

        return toResponse(administrator);
    }

    @Transactional
    public AdministratorResponse disableAdministrator(
            UUID administratorId,
            String authenticatedEmail) {

        Administrator currentAdmin = getAuthenticatedAdministrator(
                authenticatedEmail
        );

        if (currentAdmin.getId().equals(administratorId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot disable your own administrator account."
            );
        }

        Administrator administrator = administratorRepository
                .findById(administratorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Administrator not found."
                ));

        administrator.disable();

        auditLogService.record(
                "ADMINISTRATOR",
                currentAdmin.getId(),
                AuditActions.ADMINISTRATOR_DISABLED,
                "ADMINISTRATOR",
                administrator.getId(),
                Map.of(
                        "email", administrator.getEmail(),
                        "previousStatus", "ACTIVE",
                        "newStatus", "DISABLED"
                )
        );

        return toResponse(administrator);
    }

    @Transactional
    public AdministratorResponse enableAdministrator(
            UUID administratorId,
            String authenticatedEmail) {

        Administrator currentAdmin = getAuthenticatedAdministrator(
                authenticatedEmail
        );

        Administrator administrator = administratorRepository
                .findById(administratorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Administrator not found."
                ));

        if (currentAdmin.getId().equals(administratorId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot enable your own administrator account."
            );
        }

        if (administrator.getStatus() == AdministratorStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Administrator account is already active."
            );
        }

        administrator.enable();

        auditLogService.record(
                "ADMINISTRATOR",
                currentAdmin.getId(),
                AuditActions.ADMINISTRATOR_ENABLED,
                "ADMINISTRATOR",
                administrator.getId(),
                Map.of(
                        "email", administrator.getEmail(),
                        "previousStatus", "DISABLED",
                        "newStatus", "ACTIVE"
                )
        );

        return toResponse(administrator);
    }

    @Transactional
    public void recordSuccessfulAuthentication(String email) {

        Administrator administrator = administratorRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated administrator not found."
                ));

        if (administrator.getStatus() != AdministratorStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Administrator account is disabled."
            );
        }

        administrator.recordLogin();
    }
}