package com.fonseca.algashop.billing.application.invoice.management;

import com.fonseca.algashop.billing.domain.model.invoice.PaymentMethod;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private PaymentMethod method;
    private UUID creditCardId;
}
