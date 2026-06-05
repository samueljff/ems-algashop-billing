package com.fonseca.algashop.billing.infrastructure.persistence.creditCard;

import com.fonseca.algashop.billing.application.creditCard.query.CreditCardOutput;
import com.fonseca.algashop.billing.application.creditCard.query.CreditCardQueryService;
import com.fonseca.algashop.billing.application.utility.Mapper;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardNotFoundException;
import com.fonseca.algashop.billing.domain.model.creditcard.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditCardQueryServiceImpl implements CreditCardQueryService {

    private final CreditCardRepository creditCardRepository;
    private final Mapper mapper;

    @Override
    public CreditCardOutput findOne(UUID customerId, UUID creditCardId) {
        return creditCardRepository.findByCustomerIdAndId(customerId, creditCardId)
                .map(c-> mapper.convert(c, CreditCardOutput.class))
                .orElseThrow(()-> new CreditCardNotFoundException());
    }

    @Override
    public List<CreditCardOutput> findByCustomer(UUID customerId) {
        return creditCardRepository.findAllByCustomerId(customerId)
                .stream().map(c-> mapper.convert(c, CreditCardOutput.class))
                .toList();
    }
}