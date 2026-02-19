package com.fonseca.algashop.billing.domain.model.invoice;

import com.fonseca.algashop.billing.domain.model.DomainException;
import com.fonseca.algashop.billing.domain.model.InvoiceTestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class InvoiceTest {

    private static final Logger log = LoggerFactory.getLogger(InvoiceTest.class);

    // ===============================
    // Criação de Invoice (issue)
    // ===============================

    @Test
    @DisplayName("Deve criar invoice com dados corretos e status UNPAID")
    void shouldIssueInvoiceCorrectly() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();

        assertThat(invoice.getId()).isNotNull();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        assertThat(invoice.getIssuedAt()).isNotNull();
        assertThat(invoice.getExpiresAt()).isNotNull();
        assertThat(invoice.getPaidAt()).isNull();
        assertThat(invoice.getCanceledAt()).isNull();
        assertThat(invoice.getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar invoice com itens vazios")
    void shouldThrowExceptionWhenItemsEmpty() {

        Set<LineItem> emptyItems = new HashSet<>();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            InvoiceTestDataBuilder.anInvoice()
                    .items(emptyItems)
                    .build();
        });
    }

    // ===============================
    // Cenários de Sucesso
    // ===============================

    @Test
    @DisplayName("Deve marcar invoice como paga")
    void shouldMarkAsPaid() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();

        invoice.markAsPaid();

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
        assertThat(invoice.isPaid()).isTrue();
    }

    @Test
    @DisplayName("Deve cancelar invoice corretamente")
    void shouldCancelInvoice() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();

        invoice.cancel("Motivo teste");

        log.info(invoice.getCancelReason());

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELED);
        assertThat(invoice.getCanceledAt()).isNotNull();
        assertThat(invoice.getCancelReason()).isEqualTo("Motivo teste");
        assertThat(invoice.isCanceled()).isTrue();
    }

    @Test
    @DisplayName("Deve alterar configurações de pagamento")
    void shouldChangePaymentSettings() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();
        UUID cardId = UUID.randomUUID();

        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, cardId);

        assertThat(invoice.getPaymentSettings()).isNotNull();
        assertThat(invoice.getPaymentSettings().getPaymentMethod())
                .isEqualTo(PaymentMethod.GATEWAY_BALANCE);
        assertThat(invoice.getPaymentSettings().getCreditCardId())
                .isEqualTo(cardId);
    }

    @Test
    @DisplayName("Deve atribuir código do gateway")
    void shouldAssignGatewayCode() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .paymentSettings(PaymentMethod.CREDIT_CARD, UUID.randomUUID())
                .build();

        invoice.assignPaymentGatewayCode("GATEWAY123");

        assertThat(invoice.getPaymentSettings().getGatewayCode())
                .isEqualTo("GATEWAY123");
    }

    // ===============================
    // Cenários de Exceção
    // ===============================

    @Test
    @DisplayName("Não deve marcar invoice cancelada como paga")
    void shouldThrowDomainException_whenMarkCanceledInvoiceAsPaid() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .status(InvoiceStatus.CANCELED)
                .build();

        assertThatThrownBy(invoice::markAsPaid)
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Não deve cancelar invoice já cancelada")
    void shouldThrowDomainException_whenCancelAlreadyCanceledInvoice() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .status(InvoiceStatus.CANCELED)
                .build();

        assertThatThrownBy(() -> invoice.cancel("Outro motivo"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Não deve alterar payment settings de invoice paga")
    void shouldThrowDomainException_whenChangingPaymentSettingsOfPaidInvoice() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .status(InvoiceStatus.PAID)
                .build();

        assertThatExceptionOfType(DomainException.class).isThrownBy(() -> {
            invoice.changePaymentSettings(
                    PaymentMethod.CREDIT_CARD,
                    UUID.randomUUID()
            );
        });
    }

    @Test
    @DisplayName("Não deve atribuir gateway code em invoice paga")
    void shouldThrowDomainException_whenAssignGatewayCodeToAlreadyPaidInvoice() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice()
                .status(InvoiceStatus.PAID)
                .paymentSettings(PaymentMethod.CREDIT_CARD, UUID.randomUUID())
                .build();

        assertThatThrownBy(() ->
                invoice.assignPaymentGatewayCode("CODE123")
        )
                .isInstanceOf(DomainException.class);
    }

    // ===============================
    // Imutabilidade da coleção
    // ===============================

    @Test
    @DisplayName("Coleção de itens deve ser imutável")
    void itemsShouldBeImmutable() {

        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();

        assertThatThrownBy(() ->
                invoice.getItems().clear()
        )
                .isInstanceOf(UnsupportedOperationException.class);
    }
}