package com.zempay.authentication.security;

import com.zempay.authentication.entity.MerchantApiKey;
import com.zempay.authentication.entity.MerchantApiKeyStatus;
import com.zempay.authentication.repository.MerchantApiKeyRepository;
import com.zempay.authentication.service.MerchantApiKeyHashService;
import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.entity.MerchantStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class MerchantApiKeyAuthenticationProvider
        implements AuthenticationProvider {

    private final MerchantApiKeyRepository apiKeyRepository;
    private final MerchantApiKeyHashService hashService;

    public MerchantApiKeyAuthenticationProvider(
            MerchantApiKeyRepository apiKeyRepository,
            MerchantApiKeyHashService hashService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.hashService = hashService;
    }

    @Override
    @Transactional
    public MerchantApiKeyAuthenticationToken authenticate(
            org.springframework.security.core.Authentication authentication
    ) {
        String presentedKey = (String) authentication.getCredentials();

        if (presentedKey == null || presentedKey.isBlank()) {
            throw new BadCredentialsException("Invalid API key.");
        }

        String keyPrefix = extractKeyPrefix(presentedKey);

        MerchantApiKey apiKey = apiKeyRepository.findByKeyPrefix(keyPrefix)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid API key.")
                );

        if (apiKey.getStatus() != MerchantApiKeyStatus.ACTIVE) {
            throw new BadCredentialsException("Invalid API key.");
        }

        if (apiKey.getExpiresAt() != null
                && !apiKey.getExpiresAt().isAfter(Instant.now())) {
            throw new BadCredentialsException("Invalid API key.");
        }

        if (!hashService.matches(presentedKey, apiKey.getKeyHash())) {
            throw new BadCredentialsException("Invalid API key.");
        }

        Merchant merchant = apiKey.getMerchant();

        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new BadCredentialsException("Invalid API key.");
        }

        apiKey.markUsed();

        MerchantPrincipal principal = new MerchantPrincipal(
                merchant.getId(),
                merchant.getMerchantCode(),
                merchant.getDisplayName(),
                apiKey.getEnvironment()
        );

        return MerchantApiKeyAuthenticationToken.authenticated(principal);
    }

    private String extractKeyPrefix(String apiKey) {
        String[] parts = apiKey.split("_", 4);

        if (parts.length != 4 || !"zmp".equals(parts[0])) {
            throw new BadCredentialsException("Invalid API key.");
        }

        return parts[0] + "_" + parts[1] + "_" + parts[2];
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MerchantApiKeyAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}