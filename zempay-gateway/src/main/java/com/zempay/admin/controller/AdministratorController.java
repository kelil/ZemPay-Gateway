package com.zempay.admin.controller;

import com.zempay.admin.dto.AdministratorCreateRequest;
import com.zempay.admin.dto.AdministratorResponse;
import com.zempay.admin.entity.Administrator;
import com.zempay.admin.service.AdministratorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/administrators")
public class AdministratorController {

    private final AdministratorService administratorService;

    public AdministratorController(
            AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdministratorResponse createAdministrator(
            @Valid @RequestBody AdministratorCreateRequest request,
            Principal principal) {

        Administrator actor = administratorService
                .getAuthenticatedAdministrator(principal.getName());

        return administratorService.createAdministrator(
                request,
                actor.getId()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<AdministratorResponse> getAdministrators(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return administratorService.getAdministrators(pageable);
    }

    @GetMapping("/{administratorId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdministratorResponse getAdministratorById(
            @PathVariable UUID administratorId) {

        return administratorService.getAdministratorById(administratorId);
    }

    @PatchMapping("/{administratorId}/disable")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdministratorResponse disableAdministrator(
            @PathVariable UUID administratorId,
            Principal principal) {

        return administratorService.disableAdministrator(
                administratorId,
                principal.getName()
        );
    }

    @PatchMapping("/{administratorId}/enable")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AdministratorResponse enableAdministrator(
            @PathVariable UUID administratorId,
            Principal principal) {

        return administratorService.enableAdministrator(
                administratorId,
                principal.getName()
        );
    }
}