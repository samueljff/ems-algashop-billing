package com.fonseca.algashop.billing.domain.model.invoice.payment;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    FAILED,
    REFUNDED,
    PAID
}
