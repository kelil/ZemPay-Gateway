package com.zempay.authentication.service;

import com.zempay.authentication.entity.MerchantApiKeyEnvironment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class MerchantApiKeyGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public GeneratedApiKey generate(MerchantApiKeyEnvironment environment) {
        String environmentPrefix = environment.name().toLowerCase();

        String prefixRandom = generateRandomToken(6);
        String secret = generateRandomToken(32);

        String keyPrefix = "zmp_" + environmentPrefix + "_" + prefixRandom;
        String apiKey = keyPrefix + "_" + secret;

        return new GeneratedApiKey(keyPrefix, apiKey);
    }

    private String generateRandomToken(int numberOfBytes) {
        byte[] randomBytes = new byte[numberOfBytes];
        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public record GeneratedApiKey(
            String keyPrefix,
            String apiKey
    ) {
    }
}