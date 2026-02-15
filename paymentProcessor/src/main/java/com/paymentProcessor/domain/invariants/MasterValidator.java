package com.paymentProcessor.domain.invariants;

import com.paymentProcessor.domain.PaymentDto;

import java.util.ArrayList;
import java.util.List;

public class MasterValidator {
    public static List<String> preProcess(PaymentDto paymentDto){
        List<String> list=new ArrayList<>();
        list.addAll(new AmountValidator().validate(paymentDto));
        list.addAll(new CardValidator().validate(paymentDto));
        return list;
    }
}
