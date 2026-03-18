package com.rentify.rentify_api.payment.entity;

public enum PaymentEventType {
    PAYMENT_CREATED,        // 결제 건 생성
    PAYMENT_COMPLETED,      // 결제 최종 완료
    PAYMENT_FAILED,         // 결제 최종 실패
    REFUND_REQUESTED,       // 환불 요청
    REFUND_COMPLETED        // 환불 완료
}
