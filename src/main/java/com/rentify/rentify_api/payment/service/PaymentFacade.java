package com.rentify.rentify_api.payment.service;

import com.rentify.rentify_api.common.exception.DuplicateException;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.payment.entity.PaymentFailReason;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final MockPgClient mockPgClient;

    public Long processPayment(Long userId, @Valid PaymentRequest request) {
        Payment pendingPayment = paymentService.createPendingPayment(userId, request);
        Long paymentId = pendingPayment.getId();
        boolean isPgSuccess = mockPgClient.approvePayment(
            paymentId, request.getExpectedAmount()
        );

        if (!isPgSuccess) {
            paymentService.failPayment(paymentId, PaymentFailReason.PG_REJECTED);
            throw new RuntimeException("결제 승인에 실패했습니다.");
        }

        try {
            paymentService.completePayment(paymentId, userId, request);
            return paymentId;
        } catch (OptimisticLockException | PessimisticLockException e) {
            log.error("내부 처리 중 에러 발생, PG 결제를 취소합니다.", e);

            mockPgClient.cancelPayment(paymentId);
            paymentService.failPayment(paymentId, PaymentFailReason.CONCURRENCY_CONFLICT);
            throw new DuplicateException("물품이 이미 대여되었습니다. 결제가 취소됩니다.");
        } catch (Exception e) {
            log.error("시스템 오류로 인한 결제 실패", e);
            mockPgClient.cancelPayment(paymentId);
            paymentService.failPayment(paymentId, PaymentFailReason.INTERNAL_SYSTEM_ERROR);
            throw e;
        }
    }

    public Long processPaymentCancel(Long userId, Long paymentId) {
        paymentService.validatePaymentForCancel(userId, paymentId);
        mockPgClient.cancelPayment(paymentId);
        paymentService.cancelPayment(paymentId);
        return paymentId;
    }
}
