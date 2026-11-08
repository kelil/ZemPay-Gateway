package com.zempay.authentication.controller;

import com.zempay.admin.service.AdministratorService;
import com.zempay.authentication.dto.MerchantApiKeyCreateRequest;
import com.zempay.authentication.dto.MerchantApiKeyResponse;
import com.zempay.authentication.service.MerchantApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/merchants/{merchantId}/api-keys")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class MerchantApiKeyAdministrationController {

    private final MerchantApiKeyService apiKeyService;
    private final AdministratorService administratorService;

    public MerchantApiKeyAdministrationController(
            MerchantApiKeyService apiKeyService, AdministratorService administratorService
    ) {
        this.apiKeyService = apiKeyService;
        this.administratorService = administratorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantApiKeyResponse createApiKey(
            @PathVariable UUID merchantId,
            @Valid @RequestBody MerchantApiKeyCreateRequest request, Principal principal
    ) {
        UUID administratorId = administratorService
                .getAuthenticatedAdministrator(principal.getName())
                .getId();
        return apiKeyService.createApiKey(merchantId, request, administratorId);
    }

    @GetMapping
    public List<MerchantApiKeyResponse> getMerchantApiKeys(
            @PathVariable UUID merchantId
    ) {
        return apiKeyService.getMerchantApiKeys(merchantId);
    }

    @PatchMapping("/{apiKeyId}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApiKey(
            @PathVariable UUID merchantId,
            @PathVariable UUID apiKeyId, Principal principal
    ) {
        UUID administratorId = administratorService
                .getAuthenticatedAdministrator(principal.getName())
                .getId();
        apiKeyService.revokeApiKey(merchantId, apiKeyId, administratorId);
    }
}