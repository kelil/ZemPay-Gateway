package com.zempay.merchant.controller;

import com.zempay.admin.service.AdministratorService;
import com.zempay.merchant.service.MerchantService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/merchants")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class MerchantAdministrationController {

    private final MerchantService merchantService;
    private final AdministratorService administratorService;

    public MerchantAdministrationController(
            MerchantService merchantService,
            AdministratorService administratorService) {
        this.merchantService = merchantService;
        this.administratorService = administratorService;
    }

    @PostMapping("/{merchantId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveMerchant(
            @PathVariable UUID merchantId,
            Principal principal) {

        UUID administratorId = administratorService
                .getAuthenticatedAdministrator(principal.getName())
                .getId();

        merchantService.approveMerchant(merchantId, administratorId);
    }


    @PostMapping("/{merchantId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectMerchant(
            @PathVariable UUID merchantId,
            Principal principal) {

        UUID administratorId = administratorService
                .getAuthenticatedAdministrator(principal.getName())
                .getId();

        merchantService.rejectMerchant(merchantId, administratorId);
    }

    @PostMapping("/{merchantId}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspendMerchant(
            @PathVariable UUID merchantId,
            Principal principal) {

        UUID administratorId = administratorService
                .getAuthenticatedAdministrator(principal.getName())
                .getId();

        merchantService.suspendMerchant(merchantId, administratorId);
    }
}