package com.rentify.rentify_api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean revoked;

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public void revoke() {
        this.revoked = true;
    }

    public void updateExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
