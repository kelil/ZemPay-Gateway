package com.zempay.payment.repository;

import com.zempay.payment.entity.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentStatusHistoryRepository
        extends JpaRepository<PaymentStatusHistory, UUID> {

    List<PaymentStatusHistory> findAllByPayment_IdOrderByCreatedAtAsc(
            UUID paymentId
    );
}