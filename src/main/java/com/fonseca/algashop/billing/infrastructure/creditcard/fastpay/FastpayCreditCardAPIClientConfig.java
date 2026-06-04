package com.fonseca.algashop.billing.infrastructure.creditcard.fastpay;

import com.fonseca.algashop.billing.infrastructure.payment.AlgaShopPaymentProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FastpayCreditCardAPIClientConfig {

    @Bean
    public FastPayCreditCardAPIClient fastPayCreditCardAPIClient(
        AlgaShopPaymentProperties properties
    ) {
        var fastpayProperties = properties.getFastpay();

        RestClient restClient = RestClient.builder()
            .baseUrl(fastpayProperties.getHostname())
            .requestInterceptor(((request, body, execution) -> {
                request.getHeaders().add("Token", fastpayProperties.getPrivateToken());
                return execution.execute(request, body);
            })).build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(FastPayCreditCardAPIClient.class);
    }
}
