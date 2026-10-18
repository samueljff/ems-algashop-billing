package com.fonseca.algashop.billing.infrastructure.payment.fastpay;

import com.fonseca.algashop.billing.domain.model.InvoiceTestDataBuilder;
import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import com.fonseca.algashop.billing.domain.model.invoice.payment.Payment;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.fonseca.algashop.billing.infrastructure.AbstractFastpayIT;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentGatewayServiceFastpayFailureImplIT extends AbstractFastpayIT {

    @Autowired
    private PaymentGatewayServiceFastpayImpl paymentGatewayServiceFastpay;

    @BeforeAll
    public static void beforeAll(){
        startMock();
    }

    @AfterAll
    public static void afterAll(){
        stopMock();
    }

    @Test
    public void shouldThrowGatewayTimeoutException_whenFastpayIsUnavailable() {
        stopMock();

        try {
            PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(PaymentMethod.GATEWAY_BALANCE)
                .amount(new BigDecimal("1000.00"))
                .invoiceId(UUID.randomUUID())
                .payer(InvoiceTestDataBuilder.aPayer())
                .build();

            Assertions.assertThatThrownBy(() -> paymentGatewayServiceFastpay.capture(request))
                .isInstanceOf(GatewayTimeoutException.class);
        } finally {
            startMock();
        }
    }

    @Test
    public void shouldThrowGatewayTimeoutException_whenFastpayIsUnavailable_OnFindByCode() {
        UUID invoiceId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
            .paymentMethod(PaymentMethod.GATEWAY_BALANCE)
            .amount(new BigDecimal("1000.00"))
            .invoiceId(invoiceId)
            .payer(InvoiceTestDataBuilder.aPayer())
            .build();

        Payment payment = paymentGatewayServiceFastpay.capture(request);
        stopMock();

        try {
            Assertions.assertThatThrownBy(() -> paymentGatewayServiceFastpay.findByCode(payment.getGateWayCode()))
                .isInstanceOf(GatewayTimeoutException.class);
        } finally {
            startMock();
        }
    }
}