package com.fonseca.algashop.billing.infrastructure.persistence.invoice;

import com.fonseca.algashop.billing.application.invoice.query.InvoiceOutPut;
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
    public InvoiceOutPut findByOrderId(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId).orElseThrow(() -> new InvoiceNotFoundException());
        return mapper.convert(invoice, InvoiceOutPut.class);
    }
}
