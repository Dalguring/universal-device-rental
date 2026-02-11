package com.rentify.rentify_api.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async("taskExecutor")
    public void sendAuthCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Uni-Rental 회원가입 인증번호");
            message.setText("인증 번호는 [" + code + "] 입니다");

            mailSender.send(message);
            log.info("인증 메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", to, e);
        }
    }
}
