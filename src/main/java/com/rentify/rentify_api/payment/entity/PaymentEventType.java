package com.rentify.rentify_api.payment.entity;

public enum PaymentEventType {
    // 결제 생성 흐름
    PAYMENT_CREATED,        // 결제 건 생성
    POINT_DEDUCTED,         // 포인트 차감 성공
    POINT_DEDUCT_FAILED,    // 포인트 차감 실패
    COUPON_APPLIED,         // 쿠폰 적용 성공
    COUPON_APPLY_FAILED,    // 쿠폰 적용 실패
    MOCK_PAY_REQUESTED,     // Mock 결제 요청
    MOCK_PAY_SUCCESS,       // Mock 결제 성공
    MOCK_PAY_FAILED,        // Mock 결제 실패
    MOCK_PAY_TIMEOUT,       // Mock 결제 타임아웃
    PAYMENT_COMPLETED,      // 결제 최종 완료

    // 보상 트랜잭션
    POINT_RESTORED,         // 포인트 복원 (보상)
    COUPON_RESTORED,        // 쿠폰 복원 (보상)
    PAYMENT_FAILED,         // 결제 최종 실패

    // 환불 흐름
    REFUND_REQUESTED,       // 환불 요청
    REFUND_COMPLETED        // 환불 완료
}
