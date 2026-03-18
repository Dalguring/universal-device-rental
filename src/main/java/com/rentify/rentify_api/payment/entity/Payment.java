package com.rentify.rentify_api.payment.entity;

import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "point_used_amount", nullable = false)
    @Builder.Default
    private Integer usedPoint = 0;

    @Column(name = "coupon_discount_amount", nullable = false)
    @Builder.Default
    private Integer couponDiscount = 0;

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "fail_reason")
    private PaymentFailReason failReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime cancelAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateAt;

    public void updateAsPaid() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태에서만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void updateAsFailed(PaymentFailReason reason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = reason;
    }

    public void updateAsCanceled() {
        if (this.status != PaymentStatus.PAID) {
            throw new IllegalStateException("결제 완료 상태에서만 취소 가능합니다.");
        }
        this.status = PaymentStatus.CANCELED;
    }

    public void updateAsRefunded() {
        if (this.status != PaymentStatus.CANCELED) {
            throw new IllegalStateException("결제 취소 상태에서만 환불 가능합니다.");
        }
        this.status = PaymentStatus.REFUNDED;
    }
}
