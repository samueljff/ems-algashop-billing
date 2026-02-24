package com.fonseca.algashop.billing.application.invoice.query;

public interface InvoiceQueryService {
    InvoiceOutPut findByOrderId(String orderId);
}
