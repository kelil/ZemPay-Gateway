package com.zempay.authentication.service;

import com.zempay.audit.AuditActions;
import com.zempay.audit.service.AuditLogService;
import com.zempay.authentication.dto.MerchantApiKeyCreateRequest;
import com.zempay.authentication.dto.MerchantApiKeyResponse;
import com.zempay.authentication.entity.MerchantApiKey;
import com.zempay.authentication.entity.MerchantApiKeyStatus;
import com.zempay.authentication.repository.MerchantApiKeyRepository;
import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.entity.MerchantStatus;
import com.zempay.merchant.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MerchantApiKeyService {

    private final MerchantApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantApiKeyGenerator keyGenerator;
    private final MerchantApiKeyHashService hashService;
    private final AuditLogService auditLogService;

    public MerchantApiKeyService(
            MerchantApiKeyRepository apiKeyRepository,
            MerchantRepository merchantRepository,
            MerchantApiKeyGenerator keyGenerator,
            MerchantApiKeyHashService hashService, AuditLogService auditLogService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.merchantRepository = merchantRepository;
        this.keyGenerator = keyGenerator;
        this.hashService = hashService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public MerchantApiKeyResponse createApiKey(
            UUID merchantId,
            MerchantApiKeyCreateRequest request,  UUID administratorId
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Merchant not found."
                ));

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "API keys can only be created for active merchants."
            );
        }

        Instant expiresAt = request.expiresAt();

        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiration date must be in the future."
            );
        }

        MerchantApiKeyGenerator.GeneratedApiKey generatedKey;

        do {
            generatedKey = keyGenerator.generate(request.environment());
        } while (apiKeyRepository.existsByKeyPrefix(
                generatedKey.keyPrefix()
        ));

        MerchantApiKey apiKey = new MerchantApiKey(
                merchant,
                generatedKey.keyPrefix(),
                hashService.hash(generatedKey.apiKey()),
                request.environment(),
                expiresAt
        );

        MerchantApiKey savedKey = apiKeyRepository.save(apiKey);

        auditLogService.record(
                "ADMINISTRATOR",
                administratorId,
                AuditActions.MERCHANT_API_KEY_CREATED,
                "MERCHANT_API_KEY",
                savedKey.getId(),
                Map.of(
                        "merchantId", merchant.getId().toString(),
                        "keyPrefix", savedKey.getKeyPrefix(),
                        "environment", savedKey.getEnvironment().name(),
                        "status", savedKey.getStatus().name()
                )
        );

        return new MerchantApiKeyResponse(
                savedKey.getId(),
                merchant.getId(),
                savedKey.getKeyPrefix(),
                generatedKey.apiKey(),
                savedKey.getEnvironment(),
                savedKey.getStatus(),
                savedKey.getExpiresAt(),
                savedKey.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<MerchantApiKeyResponse> getMerchantApiKeys(UUID merchantId) {

        if (!merchantRepository.existsById(merchantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Merchant not found."
            );
        }

        return apiKeyRepository.findAllByMerchant_Id(merchantId)
                .stream()
                .map(apiKey -> new MerchantApiKeyResponse(
                        apiKey.getId(),
                        merchantId,
                        apiKey.getKeyPrefix(),
                        null,
                        apiKey.getEnvironment(),
                        apiKey.getStatus(),
                        apiKey.getExpiresAt(),
                        apiKey.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void revokeApiKey(UUID merchantId, UUID apiKeyId, UUID administratorId) {

        MerchantApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "API key not found."
                ));

        if (!apiKey.getMerchant().getId().equals(merchantId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "API key not found."
            );
        }

        MerchantApiKeyStatus previousStatus = apiKey.getStatus();

        try {
            apiKey.revoke();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    exception.getMessage()
            );
        }

        MerchantApiKey savedKey = apiKeyRepository.save(apiKey);

        auditLogService.record(
                "ADMINISTRATOR",
                administratorId,
                AuditActions.MERCHANT_API_KEY_REVOKED,
                "MERCHANT_API_KEY",
                savedKey.getId(),
                Map.of(
                        "merchantId", merchantId.toString(),
                        "keyPrefix", savedKey.getKeyPrefix(),
                        "environment", savedKey.getEnvironment().name(),
                        "previousStatus", previousStatus.name(),
                        "newStatus", savedKey.getStatus().name()
                )
        );

    }
}