package com.rentify.rentify_api.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentify.rentify_api.common.filter.JwtAuthenticationFilter;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.coupon.entity.Coupon;
import com.rentify.rentify_api.coupon.entity.CouponDiscountType;
import com.rentify.rentify_api.coupon.entity.CouponStatus;
import com.rentify.rentify_api.coupon.repository.CouponRepository;
import com.rentify.rentify_api.coupon.repository.UserCouponRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class CouponConcurrencyTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JavaMailSender mailSender;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Coupon coupon;
    private List<User> users;

    @BeforeEach
    void setUp() {
        coupon = couponRepository.save(Coupon.builder()
            .couponName("선착순 테스트 쿠폰")
            .description("동시성 테스트용 쿠폰")
            .discountValue(1000)
            .discountType(CouponDiscountType.FIXED)
            .maxDiscountAmount(1000)
            .minOrderAmount(5000)
            .totalQuantity((short) 10)
            .issuedQuantity((short) 0)
            .perUserLimit((short) 1)
            .validFrom(LocalDateTime.now().minusDays(1))
            .validUntil(LocalDateTime.now().plusDays(30))
            .status(CouponStatus.ACTIVE)
            .build());

        users = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            users.add(userRepository.save(User.builder()
                .name("유저" + i)
                .email("coupon-test-" + i + "@test.com")
                .password("password")
                .build())
            );
        }
    }

    @AfterEach
    void tearDown() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("수량 10개 쿠폰에 20명이 동시 요청 시 정확히 10명만 발급 성공")
    void concurrency_coupon_issue_should_limit_to_total_quantity() throws InterruptedException {
        // given
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            final Long userId = users.get(i).getId();
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    couponService.issueCoupon(userId, coupon.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        long issuedCount = userCouponRepository.count();
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();

        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("실제 발급 수: " + issuedCount);
        System.out.println("쿠폰 issuedQuantity: " + updatedCoupon.getIssuedQuantity());

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(10);
        assertThat(issuedCount).isEqualTo(10);
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo((short) 10);
        assertThat(updatedCoupon.getStatus()).isEqualTo(CouponStatus.INACTIVE);

        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 유저가 동시에 여러 번 요청해도 perUserLimit 만큼만 발급됨")
    void concurrency_same_user_should_respect_per_user_limit() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Long userId = users.get(0).getId();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    couponService.issueCoupon(userId, coupon.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        long userCouponCount = userCouponRepository.countByUserIdAndCouponId(userId,
            coupon.getId());

        System.out.println("같은 유저 - 성공: " + successCount.get());
        System.out.println("같은 유저 - 실패: " + failCount.get());
        System.out.println("같은 유저 발급 수: " + userCouponCount);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(userCouponCount).isEqualTo(1);

        executorService.shutdown();
    }
}