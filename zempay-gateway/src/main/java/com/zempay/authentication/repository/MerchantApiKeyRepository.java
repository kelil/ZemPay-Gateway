package com.zempay.authentication.repository;

import com.zempay.authentication.entity.MerchantApiKey;
import com.zempay.authentication.entity.MerchantApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantApiKeyRepository
        extends JpaRepository<MerchantApiKey, UUID> {

    Optional<MerchantApiKey> findByKeyPrefix(String keyPrefix);

    List<MerchantApiKey> findAllByMerchant_Id(UUID merchantId);

    List<MerchantApiKey> findAllByMerchant_IdAndStatus(
            UUID merchantId,
            MerchantApiKeyStatus status
    );

    boolean existsByKeyPrefix(String keyPrefix);
}