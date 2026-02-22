package com.fonseca.algashop.billing.domain.model.invoice.payment;

public interface PaymentGatewayService {
    Payment capture(PaymentRequest paymentRequest);
    Payment findByCode(String code);
}
