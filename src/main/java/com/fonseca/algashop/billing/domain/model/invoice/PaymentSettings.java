package com.fonseca.algashop.billing.domain.model.invoice;

import com.fonseca.algashop.billing.domain.model.IdGenerator;
import lombok.*;

import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentSettings {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID creditCardId;
    private String gatewayCode;
    private PaymentMethod paymentMethod;

    public static PaymentSettings brandNew(PaymentMethod paymentMethod, UUID creditCardId) {
        return new PaymentSettings(
                IdGenerator.generateTimeBasedUUID(),
                creditCardId,
                null,
                paymentMethod
        );
    }

    void assignGatewayCode(String gatewayCode){
        setGatewayCode(gatewayCode);
    }
}
