package com.paymentProcessor.domain.invariants;

import com.paymentProcessor.domain.PaymentDto;

import java.util.List;

public class CardValidator implements Validator{

    @Override
    public List<String> validate(PaymentDto paymentDto) {
        return List.of();
    }
}
