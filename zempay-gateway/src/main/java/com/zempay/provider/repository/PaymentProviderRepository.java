package com.zempay.provider.repository;

import com.zempay.provider.entity.PaymentProvider;
import com.zempay.provider.entity.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProviderRepository
        extends JpaRepository<PaymentProvider, UUID> {

    Optional<PaymentProvider> findByCode(String code);

    boolean existsByCode(String code);

    List<PaymentProvider> findAllByStatus(ProviderStatus status);
}