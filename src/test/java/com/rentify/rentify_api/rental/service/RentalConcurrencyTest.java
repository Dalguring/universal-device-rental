package com.rentify.rentify_api.rental.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentify.rentify_api.category.entity.Category;
import com.rentify.rentify_api.category.repository.CategoryRepository;
import com.rentify.rentify_api.common.filter.JwtAuthenticationFilter;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.post.entity.Post;
import com.rentify.rentify_api.post.entity.PostStatus;
import com.rentify.rentify_api.post.repository.PostRepository;
import com.rentify.rentify_api.rental.dto.RentalRequest;
import com.rentify.rentify_api.rental.entity.ReceiveMethod;
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

@SpringBootTest
class RentalConcurrencyTest {

    @Autowired
    RentalService rentalService;

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JavaMailSender mailSender;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private Post post;
    private List<User> users;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(User.builder()
            .name("게시자")
            .email("post-owner@test.com")
            .password("1234")
            .build());

        Category category = categoryRepository.save(Category.builder()
            .name("테스트 카테고리")
            .description("동시성 테스트")
            .build());

        post = postRepository.save(Post.builder()
            .user(owner)
            .category(category)
            .title("맥북 프로 대여")
            .description("동시성 테스트")
            .pricePerDay(10000)
            .maxRentalDays(30)
            .isParcel(true)
            .isMeetup(true)
            .status(PostStatus.AVAILABLE)
            .build());

        users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(userRepository.save(User.builder()
                .name("대여자" + i)
                .email("rental-user-" + i + "@test.com")
                .password("1234")
                .build()));
        }
    }

    @AfterEach
    void tearDown() {
        rentalRepository.deleteAll();
        postRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 게시글, 같은 기간에 10명이 동시 대여 신청 시 중복 대여 생성 여부 확인")
    void concurrency_rental_same_period_should_detect_overlap() throws InterruptedException {
        // given
        int threadCount = 10;
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

                    RentalRequest request = RentalRequest.builder()
                        .postId(post.getId())
                        .startDate(LocalDate.now().plusDays(1))
                        .endDate(LocalDate.now().plusDays(3))
                        .receiveMethod(ReceiveMethod.PARCEL)
                        .build();

                    rentalService.createRental(userId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("스레드 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        long activeRentalCount = rentalRepository.findAll().stream()
            .filter(r -> r.getStatus() == RentalStatus.REQUESTED
                || r.getStatus() == RentalStatus.CONFIRMED)
            .count();

        System.out.println("대여 신청 성공: " + successCount.get());
        System.out.println("대여 신청 실패: " + failCount.get());
        System.out.println("활성 대여 수: " + activeRentalCount);

        if (activeRentalCount > 1) {
            System.out.println("동시성 이슈 발견: 같은 기간에 " + activeRentalCount + "건의 대여가 생성됨");
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(activeRentalCount).isEqualTo(1);

        executorService.shutdown();
    }
}