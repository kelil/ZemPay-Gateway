package com.zempay.payment.entity;

public enum PaymentAttemptStatus {
    CREATED,
    SUBMITTED,
    PROCESSING,
    PENDING,
    UNKNOWN,
    SUCCEEDED,
    FAILED,
    CANCELLED
}
