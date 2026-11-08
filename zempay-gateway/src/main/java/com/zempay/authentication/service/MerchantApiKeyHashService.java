package com.zempay.authentication.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MerchantApiKeyHashService {

    private final PasswordEncoder passwordEncoder;

    public MerchantApiKeyHashService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hash(String apiKey) {
        return passwordEncoder.encode(apiKey);
    }

    public boolean matches(String apiKey, String storedHash) {
        return passwordEncoder.matches(apiKey, storedHash);
    }
}