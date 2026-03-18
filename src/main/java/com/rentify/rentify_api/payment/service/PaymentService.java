package com.rentify.rentify_api.payment.service;

import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.coupon.entity.Coupon;
import com.rentify.rentify_api.coupon.entity.UserCoupon;
import com.rentify.rentify_api.coupon.entity.UserCouponStatus;
import com.rentify.rentify_api.coupon.exception.CouponAlreadyUsedException;
import com.rentify.rentify_api.coupon.exception.CouponNotFoundException;
import com.rentify.rentify_api.coupon.exception.CouponNotValidException;
import com.rentify.rentify_api.coupon.repository.UserCouponRepository;
import com.rentify.rentify_api.payment.dto.PaymentCompletedEvent;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.entity.Payment;
import com.rentify.rentify_api.payment.entity.PaymentEvent;
import com.rentify.rentify_api.payment.entity.PaymentEventType;
import com.rentify.rentify_api.payment.entity.PaymentFailReason;
import com.rentify.rentify_api.payment.entity.PaymentStatus;
import com.rentify.rentify_api.payment.exception.PaymentNotFoundException;
import com.rentify.rentify_api.payment.repository.PaymentEventRepository;
import com.rentify.rentify_api.payment.repository.PaymentRepository;
import com.rentify.rentify_api.point.entity.PointHistory;
import com.rentify.rentify_api.point.entity.PointHistoryType;
import com.rentify.rentify_api.point.repository.PointHistoryRepository;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.exception.PostNotFoundException;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import com.rentify.rentify_api.rental.exception.RentalNotAvailableException;
import com.rentify.rentify_api.rental.exception.RentalNotFoundException;
import com.rentify.rentify_api.rental.repository.RentalRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UnauthenticatedException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final UserCouponRepository userCouponRepository;
    private final PostRepository postRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public Payment createPendingPayment(Long userId, PaymentRequest request) {
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

        Payment payment = Payment.builder()
            .user(rental.getUser())
            .rental(rental)
            .userCoupon(coupon)
            .totalAmount(rental.getTotalPrice())
            .usedPoint(request.getPointAmount())
            .couponDiscount(coupon != null ? coupon.getCoupon().getMaxDiscountAmount() : 0)
            .finalAmount(request.getExpectedAmount())
            .status(PaymentStatus.PENDING)
            .build();

        PaymentEvent paymentEvent = PaymentEvent.builder()
            .payment(payment)
            .eventType(PaymentEventType.PAYMENT_CREATED)
            .build();
        paymentEventRepository.save(paymentEvent);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void completePayment(Long paymentId, Long userId, PaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(PaymentNotFoundException::new);

        Rental rental = rentalRepository.findByIdWithPessimisticLock(request.getRentalId())
            .orElseThrow(RentalNotFoundException::new);

        Post post = postRepository.findByIdWithPessimisticLock(rental.getPost().getId())
            .orElseThrow(PostNotFoundException::new);

        if (post.getStatus() != PostStatus.AVAILABLE) {
            throw new RentalNotAvailableException();
        }

        if (request.getPointAmount() > 0) {
            User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
            user.usePoint(request.getPointAmount());

            PointHistory pointHistory = PointHistory.builder()
                .user(user)
                .rental(rental)
                .payment(payment)
                .type(PointHistoryType.SPEND)
                .amount(request.getPointAmount())
                .finalBalance(user.getPoint())
                .build();
            pointHistoryRepository.save(pointHistory);
        }

        if (request.getUserCouponId() != null) {
            UserCoupon userCoupon = userCouponRepository.findById(request.getUserCouponId())
                .orElseThrow(CouponNotFoundException::new);
            userCoupon.markAsUsed();
        }

        rental.confirm();
        payment.updateAsPaid();
        post.markAsRented();

        PaymentEvent paymentEvent = PaymentEvent.builder()
            .payment(payment)
            .eventType(PaymentEventType.PAYMENT_COMPLETED)
            .build();
        paymentEventRepository.save(paymentEvent);

        eventPublisher.publishEvent(
            new PaymentCompletedEvent(userId, paymentId, payment.getFinalAmount())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(Long paymentId, PaymentFailReason reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(PaymentNotFoundException::new);

        payment.updateAsFailed(reason);

        PaymentEvent paymentEvent = PaymentEvent.builder()
            .payment(payment)
            .eventType(PaymentEventType.PAYMENT_FAILED)
            .build();
        paymentEventRepository.save(paymentEvent);
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
            throw new RentalNotAvailableException();
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
