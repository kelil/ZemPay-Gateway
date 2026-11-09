package com.zempay.payment.repository;

import com.zempay.payment.entity.Payment;
import com.zempay.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByMerchant_IdAndMerchantReference(
            UUID merchantId,
            String merchantReference
    );

    Page<Payment> findAllByMerchant_Id(
            UUID merchantId,
            Pageable pageable
    );

    Page<Payment> findAllByMerchant_IdAndStatus(
            UUID merchantId,
            PaymentStatus status,
            Pageable pageable
    );

    boolean existsByMerchant_IdAndMerchantReference(
            UUID merchantId,
            String merchantReference
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.id = :paymentId
          AND p.merchant.id = :merchantId
        """)
    Optional<Payment> findByIdAndMerchantIdForUpdate(
            @Param("paymentId") UUID paymentId,
            @Param("merchantId") UUID merchantId
    );

}
