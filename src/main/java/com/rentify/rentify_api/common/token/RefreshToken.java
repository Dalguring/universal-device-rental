package com.rentify.rentify_api.common.token;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

public class RefreshToken {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    private LocalDateTime expiredAt;

    private boolean revoked;

    public RefreshToken(Long userId, String token, LocalDateTime expiredAt) {
        this.userId = userId;
        this.token = token;
        this.expiredAt = expiredAt;
        this.revoked = false;
    }

    public void revoke() {
        this.revoked = true;
    }
}
