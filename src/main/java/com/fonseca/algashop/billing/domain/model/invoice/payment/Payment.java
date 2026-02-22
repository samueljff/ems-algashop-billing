package com.fonseca.algashop.billing.domain.model.invoice.payment;

import com.fonseca.algashop.billing.domain.model.FieldValidations;
import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Builder
@EqualsAndHashCode
public class Payment {
    private String gateWayCode;
    private UUID invoiceId;
    private PaymentMethod method;
    private PaymentStatus status;

    public Payment(String gateWayCode, UUID invoiceId, PaymentMethod method, PaymentStatus status) {
        FieldValidations.requiresNonBlank(gateWayCode);
        Objects.requireNonNull(invoiceId);
        Objects.requireNonNull(method);
        Objects.requireNonNull(status);
        this.gateWayCode = gateWayCode;
        this.invoiceId = invoiceId;
        this.method = method;
        this.status = status;
    }
}
