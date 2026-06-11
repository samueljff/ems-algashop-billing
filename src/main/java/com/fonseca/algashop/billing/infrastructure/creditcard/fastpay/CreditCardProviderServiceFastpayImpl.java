package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardProviderService;
import com.fonseca.algashop.billing.domain.model.creditcard.LimitedCreditCard;
import com.fonseca.algashop.billing.presentation.BadGatewayException;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
@RequiredArgsConstructor
@Slf4j
public class CreditCardProviderServiceFastpayImpl implements CreditCardProviderService {

    private final FastPayCreditCardAPIClient fastPayCreditCardAPIClient;

    @Override
    public LimitedCreditCard register(UUID customerId, String tokenizedCard) {
        log.info("Registrando cartão de crédito — customerId: {}", customerId);
        FastpayCreditCardInput input = FastpayCreditCardInput.builder()
            .tokenizedCard(tokenizedCard)
            .customerCode(customerId.toString())
            .build();
        FastpayCreditCardResponse response;

        try {
            response = fastPayCreditCardAPIClient.create(input);
            log.info("Cartão registrado com sucesso — gatewayCode: {}", response.getId());
        } catch (ResourceAccessException e) {
            log.error("Timeout ao registrar cartão — customerId: {}", customerId);
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            log.error("Erro ao registrar cartão — customerId: {}, status: {}", customerId, e.getStatusCode());
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }

        return toLimitedCreditCard(response);
    }

    @Override
    public Optional<LimitedCreditCard> findById(String gatewayCode) {
        log.info("Buscando cartão — gatewayCode: {}", gatewayCode);
        FastpayCreditCardResponse response;
        try {
            response = fastPayCreditCardAPIClient.findById(gatewayCode);
            log.info("Cartão encontrado — gatewayCode: {}", gatewayCode);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cartão não encontrado — gatewayCode: {}", gatewayCode);
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Timeout ao buscar cartão — gatewayCode: {}", gatewayCode);
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar cartão — gatewayCode: {}, status: {}", gatewayCode, e.getStatusCode());
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }
        return Optional.of(toLimitedCreditCard(response));
    }

    @Override
    public void delete(String gatewayCode) {
        log.info("Deletando cartão — gatewayCode: {}", gatewayCode);
        try {
            fastPayCreditCardAPIClient.delete(gatewayCode);
            log.info("Cartão deletado com sucesso — gatewayCode: {}", gatewayCode);
        } catch (ResourceAccessException e) {
            log.error("Timeout ao deletar cartão — gatewayCode: {}", gatewayCode);
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            log.error("Erro ao deletar cartão — gatewayCode: {}, status: {}", gatewayCode, e.getStatusCode());
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }
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