package com.rentify.rentify_api.payment.entity;

import com.rentify.rentify_api.rental.entity.Rental;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

//    @OneToOne
//    @JoinColumn(name = "coupon_id")
//    private Coupon

    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "net_amount", nullable = false)
    private Integer netAmount;

    @Column(name = "point_usage", nullable = false)
    @Builder.Default
    private Boolean isPointUsed = false;

    @Column(name = "point_used_amount")
    @Builder.Default
    private Integer usedPoint = 0;

    @Column(name = "coupon_discount_amount")
    @Builder.Default
    private Integer couponDiscount = 0;

    @Column(name = "pg_company")
    private String pgCompany;

    @Column(name = "pg_tid")
    private String pgTid;

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
        this.paymentStatus = PaymentStatus.PAID;
    }

    private void updateAsFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    private void updateAsCanceled() {
        this.paymentStatus = PaymentStatus.CANCELED;
    }

    private void updateAsRefunded() {
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
}
