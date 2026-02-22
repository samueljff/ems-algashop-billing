package com.fonseca.algashop.billing.infrastructure.payment;

import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import com.fonseca.algashop.billing.domain.model.invoice.payment.Payment;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayServiceFakeImpl implements PaymentGatewayService {

    @Override
    public Payment capture(PaymentRequest paymentRequest) {
        return Payment.builder()
                .invoiceId(paymentRequest.getInvoiceId())
                .status(PaymentStatus.PAID)
                .method(paymentRequest.getPaymentMethod())
                .gateWayCode(UUID.randomUUID().toString())
                .build();
    }

    @Override
    public Payment findByCode(String code) {
        return Payment.builder()
                .invoiceId(UUID.randomUUID())
                .status(PaymentStatus.PAID)
                .method(PaymentMethod.GATEWAY_BALANCE)
                .gateWayCode(UUID.randomUUID().toString())
                .build();
    }
}
