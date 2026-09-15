package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.infrastructure.payment.AlgaShopPaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class FastpayCreditCardAPIClientConfig {

    @Bean
    public FastPayCreditCardAPIClient fastPayCreditCardAPIClient(
        AlgaShopPaymentProperties properties
    ) {
        var fastpayProperties = properties.getFastpay();

        RestClient restClient = RestClient.builder()
            .baseUrl(fastpayProperties.getHostname())
            .requestFactory(generateClientHttpRequestFactory())
            .requestInterceptor(((request, body, execution) -> {
                request.getHeaders().add("Token", fastpayProperties.getPrivateToken());
                return execution.execute(request, body);
            })).build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(FastPayCreditCardAPIClient.class);
    }

    private ClientHttpRequestFactory generateClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(7));
        factory.setConnectTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
