package com.rentify.rentify_api.user.service;

import com.rentify.rentify_api.common.exception.DuplicateException;
import com.rentify.rentify_api.common.exception.InvalidValueException;
import com.rentify.rentify_api.common.exception.NotFoundException;
import com.rentify.rentify_api.user.dto.AuthMeResponse;
import com.rentify.rentify_api.user.entity.EmailVerification;
import com.rentify.rentify_api.user.entity.User;
import com.rentify.rentify_api.user.exception.UserNotFoundException;
import com.rentify.rentify_api.user.repository.EmailVerificationRepository;
import com.rentify.rentify_api.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailService mailService;

    @Transactional(readOnly = true)
    public AuthMeResponse getMe(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return AuthMeResponse.from(user);
    }

    @Transactional
    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 가입된 계정입니다.");
        }

        String code = createRandomCode();

        EmailVerification verification = EmailVerification.builder()
            .email(email)
            .verificationCode(code)
            .expiredAt(LocalDateTime.now().plusMinutes(5))
            .isVerified(false)
            .build();

        emailVerificationRepository.save(verification);

        mailService.sendAuthCode(email, code);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findFirstByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new NotFoundException("인증 요청 내역이 없습니다."));

        if (verification.isExpired()) {
            throw new InvalidValueException("인증 시간이 만료되었습니다.");
        }

        if (!verification.getVerificationCode().equals(code)) {
            throw new InvalidValueException("인증 번호가 일치하지 않습니다.");
        }

        verification.verify();
    }

    private String createRandomCode() {
        return String.valueOf((int)(Math.random() * 899999) + 10000);
    }
}