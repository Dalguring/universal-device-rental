package com.rentify.rentify_api.payment.dto;

public record PaymentCompletedEvent(
    Long userId,
    Long paymentId,
    int finalAmount
) {}
