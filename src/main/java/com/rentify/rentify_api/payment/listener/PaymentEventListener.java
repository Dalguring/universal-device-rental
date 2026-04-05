package com.rentify.rentify_api.payment.listener;

import com.rentify.rentify_api.payment.dto.PaymentCompletedEvent;
import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.payment.exception.PaymentNotFoundException;
import com.rentify.rentify_api.payment.repository.PaymentRepository;
import com.rentify.rentify_api.point.entity.PointHistory;
import com.rentify.rentify_api.point.entity.PointHistoryType;
import com.rentify.rentify_api.point.repository.PointHistoryRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신: 사용자 {}, 최종 금액 {}",
            event.userId(), event.finalAmount()
        );

        try {
            int rewardPoint = (int) (event.finalAmount() * 0.05);

            if (rewardPoint > 0) {
                User user = userRepository.findById(event.userId())
                    .orElseThrow(UserNotFoundException::new);
                user.addPoint(rewardPoint);

                Payment payment = paymentRepository.findById(event.paymentId())
                    .orElseThrow(PaymentNotFoundException::new);

                String description = "[" + payment.getRental().getPost().getTitle() + "] 결제 완료";

                PointHistory history = PointHistory.builder()
                    .user(user)
                    .rental(payment.getRental())
                    .payment(payment)
                    .type(PointHistoryType.EARN)
                    .amount(rewardPoint)
                    .finalBalance(user.getPoint())
                    .description(description)
                    .build();
                pointHistoryRepository.save(history);
            }
        } catch (Exception e) {
            log.error("포인트 적립 중 에러 발생 (PaymentId: {})", event.paymentId(), e);
        }
    }
}
