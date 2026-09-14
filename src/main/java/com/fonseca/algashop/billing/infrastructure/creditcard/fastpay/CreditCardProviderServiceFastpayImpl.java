package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
@RequiredArgsConstructor
@Slf4j
public class CreditCardProviderServiceFastpayImpl implements CreditCardProviderService {

    private final ResilientFastPayCreditCardAPIClient fastPayCreditCardAPIClient;

    @Override
    public LimitedCreditCard register(UUID customerId, String tokenizedCard) {
        log.info("Registrando cartão de crédito — customerId: {}", customerId);
        FastpayCreditCardInput input = FastpayCreditCardInput.builder()
            .tokenizedCard(tokenizedCard)
            .customerCode(customerId.toString())
            .build();
        FastpayCreditCardResponse response = fastPayCreditCardAPIClient.register(input);
        log.info("Cartão registrado com sucesso — gatewayCode: {}", response.getId());
        return toLimitedCreditCard(response);
    }

    @Override
    public Optional<LimitedCreditCard> findById(String gatewayCode) {
        log.info("Buscando cartão — gatewayCode: {}", gatewayCode);
        return fastPayCreditCardAPIClient.findById(gatewayCode)
            .map(this::toLimitedCreditCard);
    }

    @Override
    public void delete(String gatewayCode) {
        log.info("Deletando cartão — gatewayCode: {}", gatewayCode);
        fastPayCreditCardAPIClient.delete(gatewayCode);
        log.info("Cartão deletado com sucesso — gatewayCode: {}", gatewayCode);
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