package com.rentify.rentify_api.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentFailReason {
    PG_REJECTED("PG사 결제 승인 실패"),
    INSUFFICIENT_POINT("포인트 잔액 부족"),
    CONCURRENCY_CONFLICT("재고 선점 실패"),
    INTERNAL_SYSTEM_ERROR("내부 시스템 오류");

    private final String description;
}
