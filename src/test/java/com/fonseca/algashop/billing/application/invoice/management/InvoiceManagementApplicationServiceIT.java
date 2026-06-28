package com.fonseca.algashop.billing.application.invoice.management;

import com.fonseca.algashop.billing.application.invoice.AbstractApplicationIT;
import com.fonseca.algashop.billing.domain.model.InvoiceTestDataBuilder;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCard;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardTestDataBuilder;
import com.fonseca.algashop.billing.domain.model.invoice.*;
import com.fonseca.algashop.billing.domain.model.invoice.payment.Payment;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentStatus;
import com.fonseca.algashop.billing.domain.model.listener.InvoiceEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

class InvoiceManagementApplicationServiceIT extends AbstractApplicationIT {
    @Autowired
    private InvoiceManagementApplicationService applicationService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @MockitoSpyBean
    private InvoicingService invoicingService;

    @MockitoBean
    private PaymentGatewayService paymentGatewayService;

    @MockitoSpyBean
    private InvoiceEventListener eventListener;

    @Test
    public void shouldGenerateInvoiceWithCreditCardAsPayment() {
        CreditCard creditCard = CreditCardTestDataBuilder.aCreditCard().build();
        creditCardRepository.saveAndFlush(creditCard);

        GenerateInvoiceInput input = GenerateInvoiceInputTestDataBuilder.anInput()
                .customerId(creditCard.getCustomerId())
                .build();

        input.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .creditCardId(creditCard.getId())
                        .method(PaymentMethod.CREDIT_CARD)
                        .build()
        );

        UUID invoiceId = applicationService.generate(input);

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(input.getOrderId());

        Assertions.assertThat(invoice.getVersion()).isEqualTo(0L);
        Assertions.assertThat(invoice.getCreatedAt()).isNotNull();
        Assertions.assertThat(invoice.getCreatedByUserId()).isNotNull();

        Mockito.verify(invoicingService).issue(any(), any(), any(), any());
        Mockito.verify(eventListener).listen(Mockito.any(InvoiceIssuedEvent.class));
    }

    @Test
    public void shouldGenerateInvoiceWithGatewayBalanceAsPayment() {
        UUID customerId = UUID.randomUUID();
        GenerateInvoiceInput input = GenerateInvoiceInputTestDataBuilder.anInput().build();

        input.setPaymentSettings(
                PaymentSettingsInput.builder()
                        .method(PaymentMethod.GATEWAY_BALANCE)
                        .build()
        );

        UUID invoiceId = applicationService.generate(input);

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow();

        Assertions.assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        Assertions.assertThat(invoice.getOrderId()).isEqualTo(input.getOrderId());

        Mockito.verify(invoicingService).issue(any(), any(), any(), any());
    }

    @Test
    public void shouldProcessInvoicePayment() {
        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, null);
        invoiceRepository.saveAndFlush(invoice);

        Payment payment = Payment.builder()
                .gateWayCode("12345")
                .invoiceId(invoice.getId())
                .method(invoice.getPaymentSettings().getPaymentMethod())
                .status(PaymentStatus.PAID)
                .build();
        Mockito.when(paymentGatewayService.capture(Mockito.any(PaymentRequest.class))).thenReturn(payment);

        applicationService.processPayment(invoice.getId());

        Invoice paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();

        Assertions.assertThat(paidInvoice.isPaid()).isTrue();

        Mockito.verify(paymentGatewayService).capture(Mockito.any(PaymentRequest.class));
        Mockito.verify(invoicingService).assignPayment(Mockito.any(Invoice.class), Mockito.any(Payment.class));
        Mockito.verify(eventListener).listen(Mockito.any(InvoicePaidEvent.class));

    }

    @Test
    public void shouldProcessInvoicePaymentAndCancelInvoice() {
        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, null);
        invoiceRepository.saveAndFlush(invoice);

        Payment payment = Payment.builder()
                .gateWayCode("12345")
                .invoiceId(invoice.getId())
                .method(invoice.getPaymentSettings().getPaymentMethod())
                .status(PaymentStatus.FAILED)
                .build();

        Mockito.when(paymentGatewayService.capture(Mockito.any(PaymentRequest.class))).thenReturn(payment);

        applicationService.processPayment(invoice.getId());

        Invoice paidInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();

        Assertions.assertThat(paidInvoice.isCanceled()).isTrue();

        Mockito.verify(paymentGatewayService).capture(Mockito.any(PaymentRequest.class));
        Mockito.verify(invoicingService).assignPayment(Mockito.any(Invoice.class), Mockito.any(Payment.class));

        Mockito.verify(eventListener).listen(Mockito.any(InvoiceCanceledEvent.class));
    }
}