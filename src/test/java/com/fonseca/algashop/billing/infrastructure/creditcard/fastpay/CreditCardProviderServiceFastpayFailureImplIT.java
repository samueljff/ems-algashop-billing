package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.fonseca.algashop.billing.infrastructure.AbstractFastpayIT;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CreditCardProviderServiceFastpayFailureImplIT extends AbstractFastpayIT {

    @BeforeAll
    public static void beforeAll(){
        startMock();
    }

    @AfterAll
    public static void afterAll(){
        stopMock();
    }

    @Test
    public void shouldThrowGatewayTimeoutException_whenFastpayIsUnavailable() {
        stopMock();

        try {
            Assertions.assertThatThrownBy(() -> registerCard())
                .isInstanceOf(GatewayTimeoutException.class);
        } finally {
            startMock();
        }
    }

    @Test
    public void shouldThrowGatewayTimeoutException_whenFastpayIsUnavailable_OnFindById() {
        LimitedCreditCard limitedCreditCard = registerCard();
        stopMock();

        try {
            Assertions.assertThatThrownBy(() -> creditCardProvider.findById(limitedCreditCard.getGatewayCode()))
                .isInstanceOf(GatewayTimeoutException.class);
        } finally {
            startMock();
        }
    }

    @Test
    public void shouldThrowGatewayTimeoutException_whenFastpayIsUnavailable_OnDelete() {
        LimitedCreditCard limitedCreditCard = registerCard();
        stopMock();

        try {
            Assertions.assertThatThrownBy(() -> creditCardProvider.delete(limitedCreditCard.getGatewayCode()))
                .isInstanceOf(GatewayTimeoutException.class);
        } finally {
            startMock();
        }
    }
}