package com.rentify.rentify_api.payment.controller;

import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface PaymentApiDocs {

    @PostMapping
    ResponseEntity<ApiResponse<Void>> requestPayment(
        @RequestHeader IdempotencyKey idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PaymentRequest request
    );
}
