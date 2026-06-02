package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
@RequiredArgsConstructor
public class CreditCardProviderServiceFastpayImpl implements CreditCardProviderService {

    private final FastPayCreditCardAPIClient fastPayCreditCardAPIClient;

    @Override
    public LimitedCreditCard register(UUID customerId, String tokenizedCard) {

        FastpayCreditCardInput input = FastpayCreditCardInput.builder()
            .tokenizedCard(tokenizedCard)
            .customerCode(customerId.toString())
            .build();
        FastpayCreditCardResponse response = fastPayCreditCardAPIClient.create(input);

        return toLimitedCreditCard(response);
    }

    @Override
    public Optional<LimitedCreditCard> findById(String gatewayCode) {
        FastpayCreditCardResponse response;
        try {
            response = fastPayCreditCardAPIClient.findById(gatewayCode);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
        return Optional.of(toLimitedCreditCard(response));
    }

    @Override
    public void delete(String gatewayCode) {
        fastPayCreditCardAPIClient.delete(gatewayCode);
    }

    private LimitedCreditCard toLimitedCreditCard(FastpayCreditCardResponse response) {
        return LimitedCreditCard.builder()
            .brand(response.getBrand())
            .expMonth(response.getExpMonth())
            .expYear(response.getExpYear())
            .lastNumbers(response.getLastNumbers())
            .gatewayCode(response.getId())
            .build();
    }
}