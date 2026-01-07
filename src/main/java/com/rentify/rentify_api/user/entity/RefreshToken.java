package com.rentify.rentify_api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public void revoke() {
        this.revoked = true;
    }
}
