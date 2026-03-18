package com.rentify.rentify_api.payment.service;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockPgClient {

    private final Random random = new Random();

    public boolean approvePayment(Long paymentId, int amount) {
        log.info("[Mock PG] 결제 승인 요청 진행 중... (PaymentId: {}, Amount: {})", paymentId, amount);
        simulateNetworkDelay();

        if (random.nextInt(3) == 0) {
            log.error("[Mock PG] 결제 승인 거절됨!");
            return false;
        }

        log.info("[Mock PG] 결제 승인 완료!");
        return true;
    }

    public void cancelPayment(Long paymentId) {
        log.info("[Mock PG] 기 승인된 결제 취소 요청 진행 중... (PaymentId: {})", paymentId);
        simulateNetworkDelay();
        log.info("[Mock PG] 결제 취소 완료!");
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(1000 + random.nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
