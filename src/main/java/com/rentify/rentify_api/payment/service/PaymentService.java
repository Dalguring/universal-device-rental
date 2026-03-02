package com.rentify.rentify_api.payment.service;

import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    public void requestPayment(IdempotencyKey idempotencyKey, Long userId, @Valid PaymentRequest request) {
    }

    public void verifyPayment() {
    }

    public void getMyPayments() {
    }

    public void getMyPayment() {
    }

    public void cancelPayment() {
    }
}
