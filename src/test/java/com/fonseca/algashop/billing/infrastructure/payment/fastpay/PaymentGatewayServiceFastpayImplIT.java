package com.fonseca.algashop.billing.infrastructure.payment.fastpay;

import com.fonseca.algashop.billing.domain.model.InvoiceTestDataBuilder;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCard;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@Transactional
class PaymentGatewayServiceFastpayImplIT extends AbstractFastpayIT {

    @Autowired
    private PaymentGatewayServiceFastpayImpl paymentGatewayServiceFastpay;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @BeforeAll
    public static void beforeAll(){
        startMock();
    }

    @AfterAll
    public static void afterAll(){
        stopMock();
    }

    @Test
    public void shouldProcessPaymentWithCreditCard(){
        LimitedCreditCard limitedCreditCard = registerCard();

        CreditCard creditCard = CreditCard.brandNew(
            validCustomerId,
            limitedCreditCard.getLastNumbers(),
            limitedCreditCard.getBrand(),
            limitedCreditCard.getExpMonth(),
            limitedCreditCard.getExpYear(),
            limitedCreditCard.getGatewayCode()
        );
        creditCardRepository.saveAndFlush(creditCard);

        UUID invoiceId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
            .paymentMethod(PaymentMethod.CREDIT_CARD)
            .amount(new BigDecimal("1000.00"))
            .invoiceId(invoiceId)
            .creditCardId(creditCard.getId())
            .payer(InvoiceTestDataBuilder.aPayer())
            .build();

        Payment payment = paymentGatewayServiceFastpay.capture(request);

        Assertions.assertThat(payment.getInvoiceId()).isEqualTo(invoiceId);
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
    public void shouldProcessPaymentWithGatewayBalance() {
        UUID invoiceId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
            .paymentMethod(PaymentMethod.GATEWAY_BALANCE)
            .amount(new BigDecimal("1000.00"))
            .invoiceId(invoiceId)
            .payer(InvoiceTestDataBuilder.aPayer())
            .build();

        Payment payment = paymentGatewayServiceFastpay.capture(request);

        Assertions.assertThat(payment.getInvoiceId()).isEqualTo(invoiceId);
    }

    @Test
    public void shouldFindPaymentByGatewayCode() {
        UUID invoiceId = UUID.randomUUID();

        PaymentRequest request = PaymentRequest.builder()
            .paymentMethod(PaymentMethod.GATEWAY_BALANCE)
            .amount(new BigDecimal("1000.00"))
            .invoiceId(invoiceId)
            .payer(InvoiceTestDataBuilder.aPayer())
            .build();

        Payment payment = paymentGatewayServiceFastpay.capture(request);

        Payment found = paymentGatewayServiceFastpay.findByCode(payment.getGateWayCode());

        Assertions.assertThat(found.getGateWayCode()).isEqualTo(payment.getGateWayCode());
        Assertions.assertThat(found.getStatus()).isNotNull();
        Assertions.assertThat(found.getMethod()).isNotNull();
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