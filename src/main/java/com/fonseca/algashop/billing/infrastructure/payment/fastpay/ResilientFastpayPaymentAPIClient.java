package com.fonseca.algashop.billing.infrastructure.payment.fastpay;

import com.fonseca.algashop.billing.presentation.BadGatewayException;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class ResilientFastpayPaymentAPIClient {

    private final FastpayPaymentAPIClient fastpayPaymentAPIClient;
    private final CircuitBreaker circuitBreakerNoRetry;
    private final CircuitBreaker circuitBreakerWithRetry;

    public ResilientFastpayPaymentAPIClient(FastpayPaymentAPIClient fastpayPaymentAPIClient,
                                            CircuitBreakerFactory circuitBreakerFactory) {
        this.fastpayPaymentAPIClient = fastpayPaymentAPIClient;
        this.circuitBreakerNoRetry = circuitBreakerFactory.create("fastpayCB-noRetry");
        this.circuitBreakerWithRetry = circuitBreakerFactory.create("fastpayCB-withRetry");
    }

    /**
     * Captura um pagamento. NÃO idempotente
     * reexecutar em caso de falha na resposta poderia gerar cobrança duplicada
     * ao cliente. Usa o circuito sem retry.
     */
    @ConcurrencyLimit(10)
    public FastpayPaymentModel capture(FastpayPaymentInput input) {
        try {
            return circuitBreakerNoRetry.run(() -> doCapture(input));
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    /** GET — leitura pura, idempotente. Usa o circuito com retry. */
    @ConcurrencyLimit(10)
    public FastpayPaymentModel findByCode(String gatewayCode) {
        try {
            return circuitBreakerWithRetry.run(() -> doFindByCode(gatewayCode));
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    private FastpayPaymentModel doCapture(FastpayPaymentInput input) {
        try {
            return fastpayPaymentAPIClient.capture(input);
        } catch (HttpClientErrorException e) {
            log.warn("Client error capturing payment, status: {}", e.getStatusCode());
            throw new BadGatewayException.ClientErrorException("Fastpay API Client Error", e);
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private FastpayPaymentModel doFindByCode(String gatewayCode) {
        try {
            return fastpayPaymentAPIClient.findById(gatewayCode);
        } catch (HttpClientErrorException e) {
            log.warn("Client error finding payment {}, status: {}", gatewayCode, e.getStatusCode());
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