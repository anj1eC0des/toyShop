package com.paymentProcessor.domain;

public record PaymentDto(double amount,String card,String currencyCode) {
}
