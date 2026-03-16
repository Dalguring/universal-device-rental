package com.rentify.rentify_api.payment.service;

import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.coupon.entity.Coupon;
import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.coupon.entity.UserCouponStatus;
import com.rentify.rentify_api.coupon.exception.CouponAlreadyUsedException;
import com.rentify.rentify_api.coupon.exception.CouponNotFoundException;
import com.rentify.rentify_api.coupon.exception.CouponNotValidException;
import com.rentify.rentify_api.coupon.repository.UserCouponRepository;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import com.rentify.rentify_api.rental.exception.RentalNotAvailableException;
import com.rentify.rentify_api.rental.exception.RentalNotFoundException;
import com.rentify.rentify_api.rental.repository.RentalRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UnauthenticatedException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final UserCouponRepository userCouponRepository;

    public Long requestPayment(Long userId, @Valid PaymentRequest request) {
        Rental rental = rentalRepository.findById(request.getRentalId())
            .orElseThrow(RentalNotFoundException::new);
        UserCoupon coupon = null;

        validateRental(userId, rental);

        if (request.getUserCouponId() != null) {
            coupon = userCouponRepository.findById(request.getUserCouponId())
                .orElseThrow(CouponNotFoundException::new);

            validateCoupon(userId, coupon);
        }

        if (request.getPointAmount() > 0) {
            validatePoint(userId, request);
        }

        validatePaymentAmount(rental, coupon, request.getPointAmount(), request.getExpectedAmount());

        return 0L;
    }

    public void getMyPayments() {
    }

    public void getMyPayment(String paymentId) {
    }

    public void cancelPayment(String paymentId) {
    }

    public void getPaymentEvents(String paymentId) {
    }

    private void validateRental(Long userId, Rental rental) {
        if (!Objects.equals(rental.getUser().getId(), userId)) {
            throw new UnauthenticatedException("대여자와 결제 요청자가 일치하지 않습니다.");
        }
        if (!rental.getStatus().equals(RentalStatus.REQUESTED)) {
            throw new RentalNotAvailableException("대여 불가능한 상태입니다.");
        }
    }

    private void validateCoupon(Long userId, UserCoupon coupon) {
        if (!Objects.equals(coupon.getUser().getId(), userId)) {
            throw new UnauthenticatedException("사용자가 소유하지 않은 쿠폰입니다.");
        }
        if (coupon.getStatus() == UserCouponStatus.USED) {
            throw new CouponAlreadyUsedException();
        }
        if (coupon.getStatus() == UserCouponStatus.EXPIRED) {
            throw new CouponNotValidException();
        }
    }

    private void validatePoint(Long userId, PaymentRequest request) {
        int pointAmount = request.getPointAmount();
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        int userPoint = user.getPoint();

        if (pointAmount > userPoint) {
            throw new InvalidValueException("사용 가능한 포인트를 초과했습니다.");
        }
    }

    private void validatePaymentAmount(
        Rental rental, UserCoupon userCoupon, int pointAmount, int expectedAmount
    ) {
        int originPrice = rental.getTotalPrice();
        int couponDiscount = 0;

        if (userCoupon != null) {
            Coupon coupon = userCoupon.getCoupon();
            int couponMinOrderAmount = coupon.getMinOrderAmount();

            if (originPrice < couponMinOrderAmount) {
                throw new InvalidValueException("주문 금액이 쿠폰 최소 주문 금액보다 작습니다.");
            }

            couponDiscount = coupon.getMaxDiscountAmount();
        }

        int totalDiscount = couponDiscount + pointAmount;
        int finalPrice = Math.max(originPrice - totalDiscount, 0);

        if (finalPrice != expectedAmount) {
            throw new InvalidValueException("결제 요청 금액이 변조되었거나 일치하지 않습니다.");
        }
    }
}
