package com.fonseca.algashop.billing.infrastructure.persistence.invoice;

import com.fonseca.algashop.billing.application.invoice.query.InvoiceOutput;
import com.fonseca.algashop.billing.application.invoice.query.InvoiceQueryService;
import com.fonseca.algashop.billing.application.utility.Mapper;
import com.fonseca.algashop.billing.domain.model.invoice.Invoice;
import com.fonseca.algashop.billing.domain.model.invoice.InvoiceNotFoundException;
import com.fonseca.algashop.billing.domain.model.invoice.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvoiceQueryServiceImpl implements InvoiceQueryService {

    private final InvoiceRepository invoiceRepository;
    private final Mapper mapper;

    @Override
    public InvoiceOutput findByOrderId(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseThrow(() -> new InvoiceNotFoundException());
        System.out.println(invoice.getPaymentSettings());
        System.out.println(invoice.getPaymentSettings().getPaymentMethod());
        InvoiceOutput output = mapper.convert(invoice, InvoiceOutput.class);

        System.out.println(output.getPaymentSettings());

        if (output.getPaymentSettings() != null) {
            System.out.println(output.getPaymentSettings().getPaymentMethod());
        }
        return output;
    }
}
