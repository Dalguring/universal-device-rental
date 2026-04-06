package com.rentify.rentify_api.payment.service;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.filter.JwtAuthenticationFilter;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.payment.dto.PaymentRequest;
import com.rentify.rentify_api.payment.entity.PaymentStatus;
import com.rentify.rentify_api.payment.repository.PaymentEventRepository;
import com.rentify.rentify_api.payment.repository.PaymentRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentConcurrencyTest {

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

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Post post;
    private List<User> renters;
    private List<Rental> rentals;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.builder()
            .name("소유자")
            .email("owner@test.com")
            .password("1234")
            .build());

        Category category = categoryRepository.save(Category.builder()
            .name("전자기기")
            .description("전자기기 카테고리")
            .build());

        post = postRepository.save(Post.builder()
            .user(owner)
            .category(category)
            .title("동시성 테스트 게시글")
            .description("테스트")
            .pricePerDay(10000)
            .maxRentalDays(30)
            .isParcel(true)
            .isMeetup(false)
            .status(PostStatus.AVAILABLE)
            .build());

        renters = new ArrayList<>();
        rentals = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            User renter = userRepository.save(User.builder()
                .name("대여자" + i)
                .email("renter-" + i + "@test.com")
                .password("1234")
                .build());
            renters.add(renter);

            Rental rental = rentalRepository.save(Rental.builder()
                .user(renter)
                .post(post)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .receiveMethod(ReceiveMethod.PARCEL)
                .status(RentalStatus.REQUESTED)
                .totalPrice(30000)
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
    @DisplayName("같은 게시글에 5명이 동시 결제 시 1명만 성공하고 나머지는 실패")
    void concurrency_payment_should_allow_only_one_success() throws InterruptedException {
        // given
        int threadCount = 5;
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
                        .expectedAmount(30000)
                        .pointAmount(0)
                        .build();

                    paymentFacade.processPayment(renters.get(index).getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("스레드 " + index + " 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        long paidCount = paymentRepository.findAll().stream()
            .filter(p -> p.getStatus() == PaymentStatus.PAID)
            .count();

        System.out.println("결제 성공: " + successCount.get());
        System.out.println("결제 실패: " + failCount.get());
        System.out.println("PAID 상태 결제 수: " + paidCount);
        System.out.println("게시글 상태: " + updatedPost.getStatus());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(paidCount).isEqualTo(1);
        assertThat(updatedPost.getStatus()).isEqualTo(PostStatus.RESERVED);

        executorService.shutdown();
    }
}