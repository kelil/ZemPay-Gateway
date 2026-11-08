package com.zempay.merchant.repository;

import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.entity.MerchantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByMerchantCode(String merchantCode);

    boolean existsByMerchantCode(String merchantCode);

    boolean existsByEmailIgnoreCase(String email);

    List<Merchant> findAllByStatus(MerchantStatus status);
}