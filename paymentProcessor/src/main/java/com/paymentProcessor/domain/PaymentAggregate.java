package com.paymentProcessor.domain;

import com.paymentProcessor.domain.invariants.MasterValidator;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentAggregate {
    private PaymentStatus status;
    private final double amount;
    private String currencyCode;
    private String card;
    private final LocalDateTime dateTime;
    private PaymentAggregate(double amount,String card,String currencyCode){
        this.status=PaymentStatus.INTITIALISED;
        this.amount=amount;
        this.currencyCode=currencyCode;
        this.card=card;
        this.dateTime= LocalDateTime.now();
    }
    public static PaymentAggregate create(double amount,String card,String currencyCode){
        List<String> errors= MasterValidator.preProcess(new PaymentDto(amount,card,currencyCode));
        if(!errors.isEmpty()){
            throw new IllegalStateException(String.join(",",errors));
        }
        return new PaymentAggregate(amount, card, currencyCode);
    }

    public static PaymentAggregate process(PaymentAggregate aggregate){
        if(aggregate.status!=PaymentStatus.INTITIALISED){
            throw new IllegalStateException("Only Initialised payments can be processed.");
        }
        aggregate.status=PaymentStatus.PROCESSING;
        return aggregate;
    }

    public static PaymentAggregate failed(PaymentAggregate aggregate){
        if(aggregate.status!= PaymentStatus.PROCESSING){
            throw new IllegalStateException("Only Processing payments can be failed.");
        }
        aggregate.status=PaymentStatus.FAILED;
        return aggregate;
    }
}
