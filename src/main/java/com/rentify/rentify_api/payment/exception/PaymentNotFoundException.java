package com.rentify.rentify_api.payment.exception;

import com.rentify.rentify_api.common.exception.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {

    public PaymentNotFoundException() {
        super("결제 내역이 없습니다.");
    }
}
