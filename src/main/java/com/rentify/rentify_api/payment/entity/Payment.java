package com.rentify.rentify_api.payment.entity;

import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @OneToOne
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

    @Column(name = "fail_reason")
    private String failReason;

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

    private void updateAsPaid() {
        this.status = PaymentStatus.PAID;
    }

    private void updateAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    private void updateAsCanceled() {
        this.status = PaymentStatus.CANCELED;
    }

    private void updateAsRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
}
