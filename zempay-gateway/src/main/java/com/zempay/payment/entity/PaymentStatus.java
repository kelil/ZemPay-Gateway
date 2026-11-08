package com.zempay.payment.entity;

public enum PaymentStatus {
    CREATED,
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED,
    REQUIRES_ACTION
}
