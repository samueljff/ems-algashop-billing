package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.presentation.BadGatewayException;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static com.fonseca.algashop.billing.infrastructure.config.resilience.FastpayCircuitBreakerConfig.fastpayCBNoWithRetryId;
import static com.fonseca.algashop.billing.infrastructure.config.resilience.FastpayCircuitBreakerConfig.fastpayCBWithRetryId;

@Component
@Slf4j
public class ResilientFastPayCreditCardAPIClient {

    private final FastPayCreditCardAPIClient fastPayCreditCardAPIClient;
    private final FrameworkRetryCircuitBreaker circuitBreakerNoRetry;
    private final FrameworkRetryCircuitBreaker circuitBreakerWithRetry;

    public ResilientFastPayCreditCardAPIClient(FastPayCreditCardAPIClient fastPayCreditCardAPIClient,
                                               CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory) {
        this.fastPayCreditCardAPIClient = fastPayCreditCardAPIClient;
        this.circuitBreakerNoRetry = (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create(fastpayCBNoWithRetryId);
        this.circuitBreakerWithRetry = (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create(fastpayCBWithRetryId);
    }

    /**
     * POST — cria um novo cartão a cada chamada. NÃO idempotente:
     * reexecutar em caso de falha na resposta poderia cadastrar um cartão duplicado.
     * Usa o circuito sem retry.
     */
    @ConcurrencyLimit(10)
    public FastpayCreditCardResponse register(FastpayCreditCardInput input) {
        try {
            return circuitBreakerNoRetry.run(() -> doRegister(input));
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    /** GET — leitura pura, idempotente. Usa o circuito com retry. */
    @ConcurrencyLimit(10)
    public Optional<FastpayCreditCardResponse> findById(String creditCardId) {
        try {
            return circuitBreakerWithRetry.run(() -> {
                logCircuitState("findById", circuitBreakerWithRetry);
                return doFindById(creditCardId);
            });
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    /** DELETE — repetir é seguro (idempotente na prática). Usa o circuito com retry. */
    @ConcurrencyLimit(10)
    public void delete(String creditCardId) {
        try {
            circuitBreakerWithRetry.run(() -> {
                logCircuitState("delete", circuitBreakerWithRetry);
                doDelete(creditCardId);
                return null;
            });
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    private void logCircuitState(String operation, FrameworkRetryCircuitBreaker circuitBreaker) {
        log.info("FastpayAPI CircuitBreaker [{}] state is {}", operation,
            circuitBreaker.getCircuitBreakerPolicy().getState());
    }

    private FastpayCreditCardResponse doRegister(FastpayCreditCardInput input) {
        try {
            return fastPayCreditCardAPIClient.create(input);
        } catch (HttpClientErrorException e) {
            log.warn("Client error registering card, status: {}", e.getStatusCode());
            throw new BadGatewayException.ClientErrorException("Fastpay API Client Error", e);
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private Optional<FastpayCreditCardResponse> doFindById(String creditCardId) {
        try {
            return Optional.of(fastPayCreditCardAPIClient.findById(creditCardId));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            log.warn("Client error finding card {}, status: {}", creditCardId, e.getStatusCode());
            throw new BadGatewayException.ClientErrorException("Fastpay API Client Error", e);
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private void doDelete(String creditCardId) {
        try {
            fastPayCreditCardAPIClient.delete(creditCardId);
        } catch (HttpClientErrorException.NotFound e) {
            // já removido — trata como sucesso, reforça a idempotência
        } catch (HttpClientErrorException e) {
            log.warn("Client error deleting card {}, status: {}", creditCardId, e.getStatusCode());
            throw new BadGatewayException.ClientErrorException("Fastpay API Client Error", e);
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private RuntimeException translateException(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            return new GatewayTimeoutException("Fastpay API Timeout", e);
        }
        if (e instanceof HttpServerErrorException) {
            return new BadGatewayException.ServerErrorException("Fastpay API Bad Gateway", e);
        }
        return new BadGatewayException("Fastpay API Bad Gateway", e);
    }

    private RuntimeException unwrapException(NoFallbackAvailableException e) {
        if (e.getCause() instanceof RetryException re && re.getCause() instanceof RuntimeException cause) {
            return cause;
        }
        if (e.getCause() instanceof RuntimeException cause) {
            return cause;
        }
        return e;
    }
}