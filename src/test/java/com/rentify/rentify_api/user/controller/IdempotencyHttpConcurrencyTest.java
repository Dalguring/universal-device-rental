package com.rentify.rentify_api.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.cache.type=simple",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test",
        "spring.security.oauth2.client.registration.google.client-id=test-client",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret",
        "jwt.secret=0123456789abcdef0123456789abcdef",
        "file.upload.path=./build/test-uploads",
        "file.base.url=http://localhost"
    }
)
@ActiveProfiles("test")
class IdempotencyHttpConcurrencyTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @DynamicPropertySource
    static void testDatabase(DynamicPropertyRegistry registry) {
        String url = requiredEnv("IDEMPOTENCY_TEST_DB_URL");

        if (!url.contains("_test")) {
            throw new IllegalStateException(
                "테스트 전용 데이터베이스만 사용해야 합니다."
            );
        }
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                name + " 환경변수를 설정하세요."
            );
        }
        return value;
    }

    @Test
    void sameKeyConcurrentRequests() throws Exception {
        int threadCount = 10;

        UUID idempotencyKey = UUID.randomUUID();
        String email = "idem-" + UUID.randomUUID() + "@test.com";
        String requestBody = """
            {
              "name": "테스트유저",
              "email": "%s",
              "password": "password1234"
            }
            """.formatted(email);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/users"))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<HttpResponse<String>>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                    );
                }));
            }

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<HttpResponse<String>> responses = new ArrayList<>();

            for (Future<HttpResponse<String>> future : futures) {
                responses.add(future.get(60, TimeUnit.SECONDS));
            }

            assertThat(responses)
                .allSatisfy(response ->
                    assertThat(response.statusCode())
                        .isIn(201, 409)
                );

            assertThat(responses.stream()
                .filter(response -> response.statusCode() == 201)
                .count()).isGreaterThanOrEqualTo(1);

            long userCount = userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .count();

            assertThat(userCount).isEqualTo(1);

            IdempotencyKey savedKey = idempotencyKeyRepository.findById(idempotencyKey).orElseThrow();

            assertThat(savedKey.getStatus())
                .isEqualTo(IdempotencyStatus.SUCCESS);

            assertThat(savedKey.getResponseCode())
                .isEqualTo(201);

            HttpResponse<String> replayResponse = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            assertThat(replayResponse.statusCode()).isEqualTo(201);

            String originalBody = responses.stream()
                .filter(response -> response.statusCode() == 201)
                .findFirst()
                .orElseThrow()
                .body();

            assertThat(objectMapper.readTree(replayResponse.body()))
                .isEqualTo(objectMapper.readTree(originalBody));

            long finalUserCount = userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .count();

            assertThat(finalUserCount).isEqualTo(1);
        } finally {
            executor.shutdownNow();

            idempotencyKeyRepository.deleteById(idempotencyKey);
            userRepository.findByEmail(email)
                .ifPresent(userRepository::delete);
        }
    }
}
