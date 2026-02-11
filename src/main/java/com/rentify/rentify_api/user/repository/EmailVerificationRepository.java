package com.rentify.rentify_api.user.repository;

import com.rentify.rentify_api.user.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
}
