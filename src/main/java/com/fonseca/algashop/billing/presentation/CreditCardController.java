package com.fonseca.algashop.billing.presentation;

import com.fonseca.algashop.billing.application.creditCard.management.CreditCardManagementService;
import com.fonseca.algashop.billing.application.creditCard.management.TokenizedCreditCardInput;
import com.fonseca.algashop.billing.application.creditCard.query.CreditCardOutput;
import com.fonseca.algashop.billing.application.creditCard.query.CreditCardQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardManagementService creditCardManagementService;
    private final CreditCardQueryService creditCardQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreditCardOutput register(@PathVariable UUID customerId,
                                     @RequestBody @Valid TokenizedCreditCardInput input){

        input.setCustomerId(customerId);

        UUID creditCardId = creditCardManagementService.register(input);

        return creditCardQueryService.findOne(customerId, creditCardId);
    }

    @GetMapping
    public List<CreditCardOutput> findAllByCustomer(@PathVariable UUID customerId){
        return creditCardQueryService.findByCustomer(customerId);
    }

    @GetMapping("/{creditCardId}")
    public CreditCardOutput findOne(@PathVariable UUID customerId, @PathVariable UUID creditCardId){
        return creditCardQueryService.findOne(customerId, creditCardId);
    }

    @DeleteMapping("/{creditCardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customerId, @PathVariable UUID creditCardId){
        creditCardManagementService.delete(customerId, creditCardId);
    }
}
