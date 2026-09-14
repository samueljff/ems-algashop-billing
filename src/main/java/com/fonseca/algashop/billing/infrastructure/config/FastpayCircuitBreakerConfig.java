package com.fonseca.algashop.billing.infrastructure.config;

import com.fonseca.algashop.billing.presentation.BadGatewayException;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

@Configuration
public class FastpayCircuitBreakerConfig {

    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> fastpayCustomizer() {

        // Operações idempotentes (leituras, exclusões): pode reexecutar com segurança
        RetryPolicy retryPolicy = RetryPolicy.builder()
            .maxRetries(3)
            .multiplier(2)
            .delay(Duration.ofSeconds(2))
            .includes(GatewayTimeoutException.class, BadGatewayException.ServerErrorException.class)
            .build();

        // Operações NÃO idempotentes (criar cartão, capturar pagamento):
        // uma única tentativa — reexecutar poderia duplicar cobrança/cadastro.
        RetryPolicy noRetryPolicy = RetryPolicy.builder()
            .maxRetries(0)
            .includes(GatewayTimeoutException.class, BadGatewayException.ServerErrorException.class)
            .build();

        return factory -> {
            factory.configure(builder -> builder
                .retryPolicy(retryPolicy)
                .openTimeout(Duration.ofSeconds(20))
                .resetTimeout(Duration.ofSeconds(30))
                .build(), "fastpayCB-withRetry");

            factory.configure(builder -> builder
                .retryPolicy(noRetryPolicy)
                .openTimeout(Duration.ofSeconds(20))
                .resetTimeout(Duration.ofSeconds(30))
                .build(), "fastpayCB-noRetry");
        };
    }
}