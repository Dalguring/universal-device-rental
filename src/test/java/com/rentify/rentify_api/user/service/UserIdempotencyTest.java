package com.rentify.rentify_api.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.filter.JwtAuthenticationFilter;
import com.rentify.rentify_api.common.jwt.JwtTokenProvider;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class UserIdempotencyTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 10번 같은 키로 보내면, 1번만 성공하고 나머지는 실패하거나 결과를 받아야 한다.")
    void signup_concurrency_test() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        UUID idempotencyKey = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("test-" + UUID.randomUUID() + "@test.com");
        request.setName("테스트유저");
        request.setPassword("password1234");

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger idempotencyFailCount = new AtomicInteger();
        AtomicInteger dbFailCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    userService.signup(idempotencyKey, request);
                    successCount.incrementAndGet();
                } catch (IdempotencyException e) {
                    idempotencyFailCount.incrementAndGet();
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    dbFailCount.incrementAndGet();
                    System.out.println("DB 충돌 발생: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("기타 에러: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        System.out.println("성공: " + successCount.get());
        System.out.println("멱등성 예외: " + idempotencyFailCount.get());
        System.out.println("DB 충돌: " + dbFailCount.get());

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(idempotencyFailCount.get() + dbFailCount.get()).isEqualTo(threadCount - 1);

        Long sameUserId = userService.signup(idempotencyKey, request);
        assertThat(sameUserId).isNotNull();
    }
}