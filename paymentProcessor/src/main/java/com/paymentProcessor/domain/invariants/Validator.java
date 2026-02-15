package com.paymentProcessor.domain.invariants;

import com.paymentProcessor.domain.PaymentDto;

import java.util.List;

public interface Validator {
    List<String> validate(PaymentDto paymentDto);
}
