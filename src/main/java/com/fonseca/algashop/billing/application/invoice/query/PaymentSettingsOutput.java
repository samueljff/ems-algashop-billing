package com.fonseca.algashop.billing.application.invoice.query;

import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSettingsOutput {
    private UUID id;
    private UUID creditCardId;
    private PaymentMethod method;
}
