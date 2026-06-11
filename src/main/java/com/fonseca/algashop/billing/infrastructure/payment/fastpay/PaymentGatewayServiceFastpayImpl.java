package com.fonseca.algashop.billing.infrastructure.payment.fastpay;

import com.fonseca.algashop.billing.domain.model.creditcard.CreditCard;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardRepository;
import com.fonseca.algashop.billing.domain.model.invoice.Address;
import com.fonseca.algashop.billing.domain.model.invoice.Payer;
import com.fonseca.algashop.billing.domain.model.invoice.payment.Payment;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentGatewayService;
import com.fonseca.algashop.billing.domain.model.invoice.payment.PaymentRequest;
import com.fonseca.algashop.billing.infrastructure.payment.AlgaShopPaymentProperties;
import com.fonseca.algashop.billing.presentation.BadGatewayException;
import com.fonseca.algashop.billing.presentation.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Service
@ConditionalOnProperty(name = "algashop.integrations.payment.provider", havingValue = "FASTPAY")
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceFastpayImpl implements PaymentGatewayService {

    private final FastpayPaymentAPIClient fastpayPaymentAPIClient;
    private final CreditCardRepository creditCardRepository;
    private final AlgaShopPaymentProperties algaShopPaymentProperties;

    @Override
    public Payment capture(PaymentRequest request) {
        log.info("Iniciando captura de pagamento — invoiceId: {}, method: {}", request.getInvoiceId(), request.getPaymentMethod());
        FastpayPaymentInput input = convertToInput(request);
        FastpayPaymentModel response;

        try {
            response = fastpayPaymentAPIClient.capture(input);
            log.info("Pagamento capturado com sucesso — gatewayCode: {}, status: {}", response.getId(), response.getStatus());
        } catch (ResourceAccessException e) {
            log.error("Timeout ao conectar com Fastpay — invoiceId: {}", request.getInvoiceId());
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            log.error("Erro na Fastpay API — status: {}, invoiceId: {}", e.getStatusCode(), request.getInvoiceId(), e);
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }

        return convertToPayment(response);
    }

    @Override
    public Payment findByCode(String gatewayCode) {
        log.info("Buscando pagamento no Fastpay — gatewayCode: {}", gatewayCode);
        FastpayPaymentModel response;
        try {
            response = fastpayPaymentAPIClient.findById(gatewayCode);
            log.info("Pagamento encontrado — gatewayCode: {}, status: {}", response.getId(), response.getStatus());
        } catch (ResourceAccessException e) {
            log.error("Timeout ao buscar pagamento no Fastpay — gatewayCode: {}", gatewayCode);
            throw new GatewayTimeoutException("Fastpay API Timeout", e);
        } catch (HttpClientErrorException e) {
            log.error("Erro ao buscar pagamento no Fastpay — gatewayCode: {}", gatewayCode, e);
            throw new BadGatewayException("Fastpay API Bad Gateway", e);
        }

        return convertToPayment(response);
    }

    private FastpayPaymentInput convertToInput(PaymentRequest request) {
        Payer payer = request.getPayer();
        Address address = payer.getAddress();
        var builder = FastpayPaymentInput.builder()
            .totalAmount(request.getAmount())
            .referenceCode(request.getInvoiceId().toString())
            .fullName(payer.getFullName())
            .document(payer.getDocument())
            .phone(payer.getPhone())
            .zipCode(address.getZipCode())
            .addressLine1(address.getStreet() + ", " + address.getNumber())
            .addressLine2(address.getComplement())
            .replyToUrl(algaShopPaymentProperties.getFastpay().getWebhookUrl());

        switch (request.getPaymentMethod()) {
            case CREDIT_CARD -> {
                builder.method(FastpayPaymentMethod.CREDIT.name());
                CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId())
                    .orElseThrow(() -> new CreditCardNotFoundException(request.getCreditCardId().toString()));
                builder.creditCardId(creditCard.getGatewayCode());
            }
            case GATEWAY_BALANCE -> {
                builder.method(FastpayPaymentMethod.GATEWAY_BALANCE.name());
            }
        }

        return builder.build();
    }

    private Payment convertToPayment(FastpayPaymentModel response) {
        var builder = Payment.builder()
            .gateWayCode(response.getId())
            .invoiceId(UUID.fromString(response.getReferenceCode()));

        FastpayPaymentMethod fastpayPaymentMethod;

        try {
            fastpayPaymentMethod = FastpayPaymentMethod.valueOf(response.getMethod());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown payment method: " + response.getMethod());
        }

        FastpayPaymentStatus fastpayPaymentStatus;
        try {
            fastpayPaymentStatus = FastpayPaymentStatus.valueOf(response.getStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown payment status: " + response.getStatus());
        }

        builder.method(FastpayEnumConverter.convert(fastpayPaymentMethod));
        builder.status(FastpayEnumConverter.convert(fastpayPaymentStatus));

        return builder.build();
    }
}