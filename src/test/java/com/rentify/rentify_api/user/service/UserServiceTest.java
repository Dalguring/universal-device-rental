package com.rentify.rentify_api.user.service;

import com.rentify.rentify_api.common.exception.DuplicateException;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공: 유저가 저장되고 ID가 반환된다.")
    void signup_success() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("test@test.com");
        request.setName("테스트유저");
        request.setPassword("password123");

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encodedPassword");

        User savedUser = User.builder().id(1L).email("test@test.com").build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        Long userId = userService.signup(idempotencyKey, request);

        // then
        assertThat(userId).isEqualTo(1L);

        // verify
        verify(userRepository, times(1)).save(any(User.class));
        verify(idempotencyKeyRepository, times(1)).save(any(IdempotencyKey.class));
    }

    @Test
    @DisplayName("멱등성 확인: 이미 성공적으로 처리된 요청(키)이면 저장된 ID를 바로 반환한다.")
    void signup_idempotency_success() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", 100L);

        IdempotencyKey existKey = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .status(IdempotencyStatus.SUCCESS)
                .responseBody(responseBody)
                .build();

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.of(existKey));

        // when
        Long userId = userService.signup(idempotencyKey, request);

        // then
        assertThat(userId).isEqualTo(100L);

        // verify
        verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("멱등성 예외: 이전 요청이 아직 처리 중이면 예외가 발생한다.")
    void signup_idempotency_pending() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();

        IdempotencyKey pendingKey = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .status(IdempotencyStatus.PENDING)
                .build();

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(
                Optional.of(pendingKey));

        // when & then
        assertThatThrownBy(() -> userService.signup(idempotencyKey, request))
                .isInstanceOf(IdempotencyException.class)
                .hasMessage("이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.");

        // verify
        verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("중복 이메일 예외: 이미 가입된 이메일이면 예외가 발생한다.")
    void signup_duplicate_email() {
        // given
        UUID idempotencyKey = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("test@test.com");

        given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(idempotencyKey, request))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 가입된 이메일 주소입니다.");

        // verify
        verify(userRepository, times(0)).save(any());
    }
}