package com.fonseca.algashop.billing.domain.model.invoice;

import com.fonseca.algashop.billing.domain.model.DomainException;
import com.fonseca.algashop.billing.domain.model.IdGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PaymentSettings {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private UUID creditCardId;
    private String gatewayCode;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToOne(mappedBy = "paymentSettings")
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PACKAGE)
    private Invoice invoice;

    static PaymentSettings brandNew(PaymentMethod paymentMethod, UUID creditCardId) {
        Objects.requireNonNull(paymentMethod);
        if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)){
            Objects.requireNonNull(creditCardId);
        }
        return new PaymentSettings(
                IdGenerator.generateTimeBasedUUID(),
                creditCardId,
                null,
                paymentMethod,
                null
        );
    }

    void assignGatewayCode(String gatewayCode) {
        if (StringUtils.isAllBlank(gatewayCode)) {
            throw new IllegalArgumentException("Gateway of payment Invalid!");
        }

        if (this.getGatewayCode() != null) {
            throw new DomainException("Gateway code already assign");
        }
        setGatewayCode(gatewayCode);
    }
}
