package com.rentify.rentify_api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rentify.rentify_api.common.exception.DuplicateException;
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.user.entity.EmailVerification;
import com.rentify.rentify_api.user.repository.EmailVerificationRepository;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MailService mailService;
    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("이메일 인증 코드 전송 테스트 그룹")
    class MailCodeVerification {

        @Test
        @DisplayName("이메일 인증 코드 전송 성공")
        void send_mail_verification_code_success() {
            // given
            String email = "newmail@mail.com";

            given(userRepository.existsByEmail(email)).willReturn(false);

            // when
            authService.sendVerificationCode(email);

            // then
            ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
            verify(emailVerificationRepository, times(1)).save(captor.capture());

            EmailVerification saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo(email);
            assertThat(saved.getVerificationCode()).isNotNull();
            assertThat(saved.getIsVerified()).isFalse();

            verify(mailService, times(1)).sendAuthCode(email, saved.getVerificationCode());
        }

        @Test
        @DisplayName("이미 가입된 메일 인증 요청")
        void duplicate_mail_verification_request() {
            // given
            String email = "exists@mail.com";

            given(userRepository.existsByEmail(email)).willReturn(true);

            // when
            assertThatThrownBy(() -> authService.sendVerificationCode(email))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 가입된 계정입니다.");

            verify(emailVerificationRepository, never()).save(any());
            verify(mailService, never()).sendAuthCode(any(), any());
        }
    }

    @Nested
    @DisplayName("이메일 인증 테스트 그룹")
    class MailVerification {

        @Test
        @DisplayName("이메일 인증 성공")
        void mail_verification_success() {
            // given
            String email = "abc@email.com";
            String code = "123456";
            EmailVerification verification = EmailVerification.builder()
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .verificationCode(code)
                .isVerified(false)
                .build();

            given(emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email))
                .willReturn(Optional.of(verification));

            // when
            authService.verifyEmail(email, code);

            // then
            assertThat(verification.getIsVerified()).isTrue();
        }

        @Test
        @DisplayName("이메일 인증 실패: 인증 요청 내역이 없는 경우 예외 발생")
        void mail_verification_fail_not_found() {
            // given
            String email = "exists@mail.com";

            given(emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(email, "123456"))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("이메일 인증 실패: 인증 시간이 만료된 경우 예외 발생")
        void mail_verification_fail_expired() {
            // given
            String email = "mail@mail.com";
            EmailVerification verification = EmailVerification.builder()
                .email(email)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

            given(emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email))
                .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(email, "123445"))
                .isInstanceOf(InvalidValueException.class);
        }

        @Test
        @DisplayName("이메일 인증 실패: 코드가 일치하지 않는 경우 예외 발생")
        void mail_verification_fail_wrong_code() {
            // given
            String email = "mail@mail.com";
            String wrongCode = "132332";

            EmailVerification verification = EmailVerification.builder()
                .email(email)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .verificationCode("123432")
                .build();

            given(emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email))
                .willReturn(Optional.of(verification));

            // when & then
            assertThatThrownBy(() -> authService.verifyEmail(email, wrongCode))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }
    }
}