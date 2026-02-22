package com.fonseca.algashop.billing.aplication.invoice.management;

import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSettingsInput {
    private PaymentMethod method;
    private UUID creditCardId;
}
