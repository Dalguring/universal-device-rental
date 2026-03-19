package com.rentify.rentify_api.payment.dto;

import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.payment.entity.PaymentFailReason;
import com.rentify.rentify_api.payment.entity.PaymentStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PaymentDetailResponse {

    private Long paymentId;
    private Long rentalId;
    private Long userCouponId;
    private Integer totalAmount;
    private Integer usedPoint;
    private Integer couponDiscount;
    private Integer finalAmount;
    private PaymentStatus status;
    private PaymentFailReason failReason;
    private LocalDateTime paidAt;
    private LocalDateTime cancelAt;
    private LocalDateTime refundAt;
    private LocalDateTime createAt;

    public static PaymentDetailResponse from(Payment payment) {
        return PaymentDetailResponse.builder()
            .paymentId(payment.getId())
            .rentalId(payment.getRental().getId())
            .userCouponId(payment.getUserCoupon().getId())
            .totalAmount(payment.getTotalAmount())
            .usedPoint(payment.getUsedPoint())
            .couponDiscount(payment.getCouponDiscount())
            .finalAmount(payment.getFinalAmount())
            .status(payment.getStatus())
            .failReason(payment.getFailReason())
            .paidAt(payment.getPaidAt())
            .cancelAt(payment.getCancelAt())
            .refundAt(payment.getRefundAt())
            .createAt(payment.getCreateAt())
            .build();
    }
}
