package com.fonseca.algashop.billing.domain.model.invoice;

import com.fonseca.algashop.billing.domain.model.DomainException;
import com.fonseca.algashop.billing.domain.model.invoice.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

import static com.fonseca.algashop.billing.domain.model.ErrorMessages.INVOICE_ALREADY_EXISTS_FOR_ORDER;

@Service
@RequiredArgsConstructor
public class InvoicingService {
    private final InvoiceRepository invoiceRepository;

    public Invoice issue(String orderId, UUID customerId, Payer payer, Set<LineItem> items){

        if (invoiceRepository.existsByOrderId(orderId)){
            throw new DomainException(String.format(INVOICE_ALREADY_EXISTS_FOR_ORDER, orderId));
        }
        return Invoice.issue(orderId, customerId, payer, items);
    }

    public void assignPayment(Invoice invoice, Payment payment) {
        invoice.assignPaymentGatewayCode(payment.getGateWayCode());
        switch (payment.getStatus()) {
            case FAILED -> invoice.cancel("Payment failed");
            case REFUNDED -> invoice.cancel("Payment refunded");
            case PAID -> invoice.markAsPaid();
        }
    }
}
