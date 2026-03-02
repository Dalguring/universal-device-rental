package com.rentify.rentify_api.coupon.service;

import com.rentify.rentify_api.coupon.dto.CouponResponse;
import com.rentify.rentify_api.coupon.dto.UserCouponResponse;
import com.rentify.rentify_api.coupon.entity.Coupon;
import com.rentify.rentify_api.coupon.entity.CouponStatus;
import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.coupon.entity.UserCouponStatus;
import com.rentify.rentify_api.coupon.exception.CouponIssueLimitExceededException;
import com.rentify.rentify_api.coupon.exception.CouponNotActiveException;
import com.rentify.rentify_api.coupon.exception.CouponNotFoundException;
import com.rentify.rentify_api.coupon.exception.CouponNotValidException;
import com.rentify.rentify_api.coupon.exception.CouponSoldOutException;
import com.rentify.rentify_api.coupon.repository.CouponRepository;
import com.rentify.rentify_api.coupon.repository.UserCouponRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);

        // 쿠폰 조회 (비관적 락)
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
            .orElseThrow(CouponNotFoundException::new);

        // 1) 현재 발급수량 +1이 총 발급수량 이하인 경우
        if (coupon.getIssuedQuantity() + 1 > coupon.getTotalQuantity()) {
            throw new CouponSoldOutException();
        }

        // 2) 유저가 발급받은 쿠폰 수량이 유저당제한수량 이하인 경우
        long userIssuedCount = userCouponRepository.countByUserIdAndCouponId(userId, couponId);
        if (userIssuedCount >= coupon.getPerUserLimit()) {
            throw new CouponIssueLimitExceededException();
        }

        // 3) 현재 일자가 유효시작일~유효종료일 사이인 경우
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new CouponNotValidException();
        }

        // 4) 쿠폰의 상태가 ACTIVE 인 경우
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CouponNotActiveException();
        }

        // 쿠폰 발급
        // 1) coupons 테이블의 issued_quantity를 +1 update하고, 만약 총발급수량과 일치해진다면 쿠폰의 상태도 INACTIVE로 변경
        coupon.increaseIssuedQuantity();

        // 2) user_coupons 테이블에 신규 데이터 생성
        UserCoupon userCoupon = UserCoupon.builder()
            .user(user)
            .coupon(coupon)
            .status(UserCouponStatus.AVAILABLE)
            .build();

        userCouponRepository.save(userCoupon);

        log.info("쿠폰 발급 완료 - userId: {}, couponId: {}, issuedQuantity: {}/{}",
            userId, couponId, coupon.getIssuedQuantity(), coupon.getTotalQuantity());
    }

    @Transactional(readOnly = true)
    public List<UserCouponResponse> getMyUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findAllByUserIdWithCoupon(userId);
        return userCoupons.stream()
            .map(UserCouponResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return coupons.stream()
            .map(CouponResponse::from)
            .toList();
    }
}
