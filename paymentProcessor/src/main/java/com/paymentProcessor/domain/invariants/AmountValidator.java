package com.paymentProcessor.domain.invariants;

import com.paymentProcessor.domain.PaymentDto;

import java.util.ArrayList;
import java.util.List;

public class AmountValidator implements Validator{
    @Override
    public List<String> validate(PaymentDto dto){
        double amount=dto.amount();
        List<String> errors= new ArrayList<>();
        if (amount < 0) errors.add("Amount should be at-least greater than 0.");
        if (amount > 5000) errors.add("Amount cap exceeded.");
        return errors;
    }
}
