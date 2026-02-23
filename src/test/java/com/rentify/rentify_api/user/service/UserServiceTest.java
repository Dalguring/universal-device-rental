package com.rentify.rentify_api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rentify.rentify_api.common.exception.DuplicateException;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.exception.InvalidPasswordException;
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import com.rentify.rentify_api.user.dto.CreateUserRequest;
import com.rentify.rentify_api.user.dto.PasswordUpdateRequest;
import com.rentify.rentify_api.user.dto.UserUpdateRequest;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.DuplicateEmailException;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @InjectMocks
    private UserService userService;

    private UUID idempotencyKey;

    @BeforeEach
    void setUP() {
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("회원가입 테스트 그룹")
    class UserJoin {

        @Test
        @DisplayName("회원가입 성공: 유저가 저장되고 ID가 반환된다.")
        void signup_success() {
            // given
            CreateUserRequest request = CreateUserRequest.builder()
                .email("test@test.com")
                .name("테스트유저")
                .password("password123")
                .build();

            given(idempotencyKeyRepository.findById(idempotencyKey)).willReturn(Optional.empty());
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encodedPassword");

            User savedUser = User.builder().id(1L).email("test@test.com").build();

            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(idempotencyKeyRepository.saveAndFlush(any(IdempotencyKey.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Long userId = userService.signup(idempotencyKey, request);

            // then
            assertThat(userId).isEqualTo(1L);

            // verify
            verify(userRepository, times(1)).save(any(User.class));
            verify(idempotencyKeyRepository, times(1)).saveAndFlush(any(IdempotencyKey.class));
        }

        @Test
        @DisplayName("멱등성 확인: 이미 성공적으로 처리된 요청(키)이면 저장된 ID를 바로 반환한다.")
        void signup_idempotency_success() {
            // given
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", 100L);

            IdempotencyKey existKey = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .status(IdempotencyStatus.SUCCESS)
                .responseBody(responseBody)
                .build();

            CreateUserRequest request = CreateUserRequest.builder().build();

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
            IdempotencyKey pendingKey = IdempotencyKey.builder()
                .idempotencyKey(idempotencyKey)
                .status(IdempotencyStatus.PENDING)
                .build();

            CreateUserRequest request = CreateUserRequest.builder().build();

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
            CreateUserRequest request = CreateUserRequest.builder()
                .email("test@test.com")
                .build();

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

    @Nested
    @DisplayName("패스워드 테스트 그룹")
    class UserPassword {

        @Test
        @DisplayName("패스워드 변경 성공")
        void change_password_success() {
            // given
            Long userId = 1L;
            User user = User.builder()
                .id(userId)
                .password(passwordEncoder.encode("1234"))
                .build();
            PasswordUpdateRequest request = new PasswordUpdateRequest("1234", "4321");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            userService.changePassword(userId, request);

            // then
            assertThat(passwordEncoder.matches(request.newPassword(), user.getPassword())).isTrue();
        }

        @Test
        @DisplayName("패스워드 변경 실패: 존재하지 않는 유저의 요청의 경우 실패")
        void change_password_fail_not_found_user() {
            // given
            Long userId = 1L;
            PasswordUpdateRequest request = new PasswordUpdateRequest("1234", "4321");

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("패스워드 변경 실패: 패스워드 미일치의 경우 실패")
        void change_password_fail_invalid_password() {
            // given
            User user = User.builder()
                .id(100L)
                .password(passwordEncoder.encode("1324"))
                .build();
            PasswordUpdateRequest request = new PasswordUpdateRequest("1234", "4321");

            given(userRepository.findById(any())).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.changePassword(100L, request))
                .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        @DisplayName("패스워드 변경 실패: 기존 패스워드로 변경 요청할 경우 실패")
        void change_password_fail_same_password() {
            // given
            User user = User.builder()
                .id(100L)
                .password(passwordEncoder.encode("1234"))
                .build();
            PasswordUpdateRequest request = new PasswordUpdateRequest("1234", "1234");

            given(userRepository.findById(any())).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.changePassword(100L, request))
                .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정 테스트 그룹")
    class UserInfo {

        @Test
        @DisplayName("사용자 정보 수정 성공")
        void update_user_info_success() {
            // given
            Long userId = 10L;
            User user = User.builder()
                .id(userId)
                .name("테스트유저")
                .address("주소1")
                .email("test@test.com")
                .account("12341234")
                .build();
            UserUpdateRequest request = UserUpdateRequest.builder()
                .name("수정유저")
                .email("test2@test.com")
                .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

            // when
            userService.updateUserinfo(userId, request);

            // then
            assertThat(user.getName()).isEqualTo("수정유저");
            assertThat(user.getEmail()).isEqualTo("test2@test.com");

            assertThat(user.getAddress()).isEqualTo("주소1");
            assertThat(user.getAccount()).isEqualTo("12341234");

            verify(userRepository, atLeastOnce()).findById(userId);
            verify(userRepository, atLeastOnce()).findByEmail(request.getEmail());
        }

        @Test
        @DisplayName("사용자 정보 수정 실패: 존재하지 않는 유저의 요청의 경우 실패")
        void update_user_info_fail_not_found_user() {
            // given
            UserUpdateRequest request = UserUpdateRequest.builder().build();

            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUserinfo(1L, request))
                .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("사용자 정보 수정 실패: 이미 존재하는 이메일로 수정하려는 경우 실패")
        void update_user_info_fail_already_exists_email() {
            // given
            Long userId = 10L;
            User user = User.builder()
                .id(userId)
                .email("user@mail.com")
                .build();
            User user2 = User.builder()
                .id(20L)
                .email("exists@mail.com")
                .build();
            UserUpdateRequest request = UserUpdateRequest.builder()
                .email("exists@mail.com")
                .build();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user2));

            // when & then
            assertThatThrownBy(() -> userService.updateUserinfo(userId, request))
                .isInstanceOf(DuplicateEmailException.class);

            verify(userRepository, atLeastOnce()).findByEmail(request.getEmail());
        }
    }
}