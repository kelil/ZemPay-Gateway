package com.zempay.payment.repository;

import com.zempay.payment.entity.PaymentAttempt;
import com.zempay.payment.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentAttemptRepository
        extends JpaRepository<PaymentAttempt, UUID> {

    List<PaymentAttempt> findAllByPayment_IdOrderByAttemptNumberAsc(
            UUID paymentId
    );

    Optional<PaymentAttempt> findByPayment_IdAndAttemptNumber(
            UUID paymentId,
            Integer attemptNumber
    );

    Optional<PaymentAttempt> findTopByPayment_IdOrderByAttemptNumberDesc(
            UUID paymentId
    );

    boolean existsByPayment_IdAndStatusIn(
            UUID paymentId,
            List<PaymentAttemptStatus> statuses
    );

    long countByPayment_Id(UUID paymentId);

    @Query("""
        SELECT COALESCE(MAX(a.attemptNumber), 0)
        FROM PaymentAttempt a
        WHERE a.payment.id = :paymentId
        """)
    Integer findNextAttemptNumberBase(@Param("paymentId") UUID paymentId);
}