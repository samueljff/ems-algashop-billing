package com.fonseca.algashop.billing.application.invoice.query;

import com.fonseca.algashop.billing.application.invoice.AbstractApplicationIT;
import com.fonseca.algashop.billing.domain.model.InvoiceTestDataBuilder;
import com.fonseca.algashop.billing.domain.model.invoice.Invoice;
import com.fonseca.algashop.billing.domain.model.invoice.InvoiceRepository;
import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

class InvoiceQueryServiceIT extends AbstractApplicationIT {

    @Autowired
    private InvoiceQueryService invoiceQueryService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    public void shouldFindByOrderId() {
        Invoice invoice = InvoiceTestDataBuilder.anInvoice().build();
        invoice.changePaymentSettings(PaymentMethod.GATEWAY_BALANCE, null);
        invoiceRepository.saveAndFlush(invoice);
        InvoiceOutput invoiceOutput = invoiceQueryService.findByOrderId(invoice.getOrderId());

        Assertions.assertThat(invoiceOutput.getId()).isEqualTo(invoice.getId());
    }
}