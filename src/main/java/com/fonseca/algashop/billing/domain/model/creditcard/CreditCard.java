package com.fonseca.algashop.billing.domain.model.creditcard;

import com.fonseca.algashop.billing.domain.model.IdGenerator;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Setter(AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditCard {

    @EqualsAndHashCode.Include
    private UUID id;
    private OffsetDateTime createdAt;
    private UUID customerId;
    private String lastNumbers;
    private String brand;
    private Integer expMonth;
    private Integer expYear;
    private String gatewayCode;

    public static CreditCard brandNew(UUID customerId, String lastNumbers, String brand, Integer expMonth, Integer expYear, String gatewayCode) {

        Objects.requireNonNull(customerId);
        Objects.requireNonNull(expMonth);
        Objects.requireNonNull(expYear);

        if (StringUtils.isAnyBlank(lastNumbers, brand, gatewayCode)) {
            throw new IllegalArgumentException("Invalid credit card information");
        }
        return new CreditCard(
                IdGenerator.generateTimeBasedUUID(),
                OffsetDateTime.now(),
                customerId,
                lastNumbers,
                brand,
                expMonth,
                expYear,
                gatewayCode
        );
    }

    public void setGatewayCode(String gatewayCode) {
        if (StringUtils.isBlank(gatewayCode)){
            throw new IllegalArgumentException("Invalid gateway code for credit card");
        }
        this.gatewayCode = gatewayCode;
    }
}
