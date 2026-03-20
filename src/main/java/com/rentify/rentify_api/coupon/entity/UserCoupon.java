package com.rentify.rentify_api.coupon.entity;

import com.rentify.rentify_api.coupon.exception.CouponAlreadyUsedException;
import com.rentify.rentify_api.coupon.exception.CouponNotValidException;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_coupons")
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "user_coupon_status")
    private UserCouponStatus status;

    @Version
    @Column(name = "coupon_version", nullable = false)
    @Builder.Default
    private Short couponVersion = (short) 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markAsUsed() {
        if (this.status == UserCouponStatus.USED) {
            throw new CouponAlreadyUsedException();
        }
        if (this.status == UserCouponStatus.EXPIRED) {
            throw new CouponNotValidException();
        }
        this.status = UserCouponStatus.USED;
    }

    public void markAsAvailable() {
        if (this.coupon.getValidUntil().isAfter(LocalDateTime.now())) {
            this.status = UserCouponStatus.AVAILABLE;
        }
    }
}
