package com.fonseca.algashop.billing.domain.model.invoice;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter(AccessLevel.PRIVATE)
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LineItem {
    private Integer number;
    private String name;
    private BigDecimal amount;
}
