package com.zempay.merchant.service;

import com.zempay.audit.AuditActions;
import com.zempay.audit.service.AuditLogService;
import com.zempay.merchant.dto.MerchantRegistrationRequest;
import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.entity.MerchantStatus;
import com.zempay.merchant.repository.MerchantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final AuditLogService auditLogService;

    public MerchantService(MerchantRepository merchantRepository, AuditLogService auditLogService) {
        this.merchantRepository = merchantRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Merchant registerMerchant(MerchantRegistrationRequest request) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (merchantRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "A merchant with this email already exists."
            );
        }

        Merchant merchant = new Merchant();

        merchant.setMerchantCode(generateMerchantCode());
        merchant.setBusinessName(request.businessName().trim());
        merchant.setDisplayName(request.displayName().trim());
        merchant.setEmail(email);
        merchant.setPhone(
                request.phone() == null || request.phone().isBlank()
                        ? null
                        : request.phone().trim()
        );

        // New merchants must wait for manual approval.
        // The entity default is PENDING.

        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public Merchant getMerchantById(UUID id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Merchant not found"
                ));
    }

    @Transactional(readOnly = true)
    public Page<Merchant> getMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable);
    }


    private String generateMerchantCode() {
        String code;

        do {
            code = "ZMP-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
        } while (merchantRepository.existsByMerchantCode(code));

        return code;
    }

    @Transactional
    public Merchant approveMerchant(UUID merchantId, UUID administratorId) {
        Merchant merchant = getMerchantById(merchantId);

        merchant.approve(administratorId);

        Merchant saved = merchantRepository.save(merchant);

        auditLogService.record(
                "ADMINISTRATOR",
                administratorId,
                AuditActions.MERCHANT_APPROVED,
                "MERCHANT",
                saved.getId(),
                Map.of(
                        "merchantCode", saved.getMerchantCode(),
                        "businessName", saved.getBusinessName(),
                        "previousStatus", "PENDING",
                        "newStatus", "ACTIVE"
                )
        );

        return saved;
    }

    @Transactional
    public Merchant rejectMerchant(
            UUID merchantId,
            UUID administratorId) {

        Merchant merchant = getMerchantById(merchantId);

        merchant.reject();

        Merchant saved = merchantRepository.save(merchant);

        auditLogService.record(
                "ADMINISTRATOR",
                administratorId,
                AuditActions.MERCHANT_REJECTED,
                "MERCHANT",
                saved.getId(),
                Map.of(
                        "merchantCode", saved.getMerchantCode(),
                        "businessName", saved.getBusinessName(),
                        "previousStatus", "PENDING",
                        "newStatus", "REJECTED"
                )
        );

        return saved;
    }

    @Transactional
    public Merchant suspendMerchant(
            UUID merchantId,
            UUID administratorId) {

        Merchant merchant = getMerchantById(merchantId);

        merchant.suspend();

        Merchant saved = merchantRepository.save(merchant);

        auditLogService.record(
                "ADMINISTRATOR",
                administratorId,
                AuditActions.MERCHANT_SUSPENDED,
                "MERCHANT",
                saved.getId(),
                Map.of(
                        "merchantCode", saved.getMerchantCode(),
                        "businessName", saved.getBusinessName(),
                        "previousStatus", "ACTIVE",
                        "newStatus", "SUSPENDED"
                )
        );

        return saved;
    }
}