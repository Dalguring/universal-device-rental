package com.rentify.rentify_api.point.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.filter.JwtAuthenticationFilter;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.listener.PaymentEventListener;
import com.rentify.rentify_api.payment.repository.PaymentEventRepository;
import com.rentify.rentify_api.payment.repository.PaymentRepository;
import com.rentify.rentify_api.payment.service.PaymentFacade;
import com.rentify.rentify_api.point.repository.PointHistoryRepository;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.rental.entity.ReceiveMethod;
import com.rentify.rentify_api.rental.entity.Rental;
import com.rentify.rentify_api.rental.entity.RentalStatus;
import com.rentify.rentify_api.rental.repository.RentalRepository;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDate;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class PointConcurrencyTest {

    @Autowired
    PaymentFacade paymentFacade;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentEventRepository paymentEventRepository;

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JavaMailSender mailSender;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    PaymentEventListener paymentEventListener;

    private User renter;
    private List<Rental> rentals;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.builder()
            .name("소유자")
            .email("point-owner@test.com")
            .password("1234")
            .build());

        renter = userRepository.save(User.builder()
            .name("대여자")
            .email("point-renter@test.com")
            .password("1234")
            .point(1000)
            .build());

        Category category = categoryRepository.save(Category.builder()
            .name("전자기기")
            .description("동시성 테스트")
            .build());

        List<Post> posts = new ArrayList<>();
        rentals = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Post post = postRepository.save(Post.builder()
                .user(owner)
                .category(category)
                .title("테스트게시" + i)
                .description("동시성 테스트 " + i)
                .pricePerDay(5000)
                .maxRentalDays(30)
                .isParcel(true)
                .isMeetup(false)
                .status(PostStatus.AVAILABLE)
                .build());
            posts.add(post);

            Rental rental = rentalRepository.save(Rental.builder()
                .user(renter)
                .post(post)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .receiveMethod(ReceiveMethod.PARCEL)
                .status(RentalStatus.REQUESTED)
                .totalPrice(10000)
                .build());
            rentals.add(rental);
        }
    }

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAll();
        paymentEventRepository.deleteAll();
        paymentRepository.deleteAll();
        rentalRepository.deleteAll();
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1000포인트 유저가 동시에 600포인트씩 3건 결제 시 최대 1건만 포인트 차감 성공")
    void concurrency_point_deduction_should_not_exceed_balance() throws InterruptedException {
        // given
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    PaymentRequest request = PaymentRequest.builder()
                        .rentalId(rentals.get(index).getId())
                        .expectedAmount(10000 - 600)
                        .pointAmount(600)
                        .build();

                    paymentFacade.processPayment(renter.getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("스레드 " + index + " 실패: " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        User updatedUser = userRepository.findById(renter.getId()).orElseThrow();

        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("최종 포인트: " + updatedUser.getPoint());

        assertThat(updatedUser.getPoint()).isGreaterThanOrEqualTo(0);
        assertThat(successCount.get()).isLessThanOrEqualTo(1);

        executorService.shutdown();
    }

    @Test
    @DisplayName("같은 유저의 포인트를 동시에 차감하면 @Version 낙관적 락에 의해 1건만 성공")
    void concurrency_optimistic_lock_on_point_deduction() throws InterruptedException {
        // given
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger optimisticLockFailCount = new AtomicInteger();
        AtomicInteger otherFailCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    deductPointInNewTransaction(renter.getId(), 600);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    optimisticLockFailCount.incrementAndGet();
                    System.out.println("스레드 " + index + " 낙관적 락 충돌: " + e.getMessage());
                } catch (Exception e) {
                    otherFailCount.incrementAndGet();
                    System.out.println("스레드 " + index + " 기타 실패: " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        User updatedUser = userRepository.findById(renter.getId()).orElseThrow();

        System.out.println("성공: " + successCount.get());
        System.out.println("낙관적 락 실패: " + optimisticLockFailCount.get());
        System.out.println("기타 실패: " + otherFailCount.get());
        System.out.println("최종 포인트: " + updatedUser.getPoint());
        System.out.println("포인트 버전: " + updatedUser.getPointVersion());

        assertThat(updatedUser.getPoint()).isGreaterThanOrEqualTo(0);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(optimisticLockFailCount.get()).isGreaterThanOrEqualTo(1);

        executorService.shutdown();
    }

    private void deductPointInNewTransaction(Long userId, int amount) {
        transactionTemplate.execute(status -> {
            User user = userRepository.findById(userId).orElseThrow();
            user.usePoint(amount);
            userRepository.saveAndFlush(user);
            return null;
        });
    }
}