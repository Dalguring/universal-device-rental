package com.rentify.rentify_api.coupon.repository;

import com.rentify.rentify_api.coupon.entity.UserCoupon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    @Query("SELECT COUNT(uc) FROM UserCoupon uc WHERE uc.user.id = :userId AND uc.coupon.id = :couponId")
    long countByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.id = :userId")
    List<UserCoupon> findAllByUserIdWithCoupon(@Param("userId") Long userId);
}
