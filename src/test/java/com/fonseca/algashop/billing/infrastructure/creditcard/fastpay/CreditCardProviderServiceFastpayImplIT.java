package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.fonseca.algashop.billing.infrastructure.AbstractFastpayIT;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class CreditCardProviderServiceFastpayImplIT  extends AbstractFastpayIT {

    @BeforeAll
    public static void beforeAll(){
        startMock();
    }

    @AfterAll
    public static void afterAll(){
        stopMock();
    }

    @Test
    public void shouldRegisterCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();
        Assertions.assertThat(limitedCreditCard.getGatewayCode()).isNotBlank();
    }

    @Test
    public void shouldFindRegisteredCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();

        LimitedCreditCard limitedCreditCardFound = creditCardProvider.findById(limitedCreditCard.getGatewayCode()).orElseThrow();

        Assertions.assertThat(limitedCreditCardFound.getGatewayCode()).isEqualTo(limitedCreditCard.getGatewayCode());
    }

    @Test
    public void shouldDeleteRegisteredCreditCard() {
        LimitedCreditCard limitedCreditCard = registerCard();

        creditCardProvider.delete(limitedCreditCard.getGatewayCode());
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
    public void shouldReturnEmpty_whenCreditCardNotFound() {
        Optional<LimitedCreditCard> result = creditCardProvider.findById("non-existing-gateway-code");

        Assertions.assertThat(result).isEmpty();
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