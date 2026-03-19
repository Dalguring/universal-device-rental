package com.rentify.rentify_api.payment.controller;

import com.rentify.rentify_api.common.idempotency.Idempotent;
import com.rentify.rentify_api.common.response.ApiResponse;
import com.rentify.rentify_api.payment.dto.PaymentDetailResponse;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.dto.PaymentResponse;
import com.rentify.rentify_api.payment.service.PaymentFacade;
import com.rentify.rentify_api.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApiDocs {

    private final PaymentFacade paymentFacade;
    private final PaymentService paymentService;

    @Override
    @Idempotent
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> requestPayment(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody PaymentRequest request
    ) {
        Long paymentId = paymentFacade.processPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
            HttpStatus.OK,
            "결제가 완료되었습니다.",
            new PaymentResponse(paymentId))
        );
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentDetailResponse>>> getPaymentsInfo(
        @AuthenticationPrincipal Long userId,
        @PageableDefault(sort = "createAt", direction = Direction.DESC)Pageable pageable
    ) {
        Page<PaymentDetailResponse> paymentPage = paymentService.getPaymentsInfo(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, paymentPage));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentInfo(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long id
    ) {
        PaymentDetailResponse response = paymentService.getPaymentInfo(userId, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
        @AuthenticationPrincipal Long userId,
        @PathVariable String paymentId
    ) {
        paymentService.cancelPayment(userId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }

    @GetMapping("/{paymentId}/events")
    public ResponseEntity<ApiResponse<Void>> getPaymentEvents(@PathVariable String paymentId) {
        paymentService.getPaymentEvents(paymentId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK));
    }
}
