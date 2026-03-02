package com.rentify.rentify_api.payment.controller;

import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApiDocs {

    private final PaymentService paymentService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<Void>> requestPayment(
        @RequestHeader IdempotencyKey idempotencyKey,
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PaymentRequest request
    ) {
        paymentService.requestPayment(idempotencyKey, userId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @PostMapping("/{merchant_uid}/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(@PathVariable String merchant_uid) {
        paymentService.verifyPayment();
        return ResponseEntity.ok((ApiResponse.success(HttpStatus.OK)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> getMyPayments() {
        paymentService.getMyPayments();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @GetMapping("/{payment_id}")
    public ResponseEntity<ApiResponse<Void>> getMyPayment(@PathVariable String payment_id) {
        paymentService.getMyPayment();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @PostMapping("/{payment_id}/cancellations")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(@PathVariable String payment_id) {
        paymentService.cancelPayment();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }
}
