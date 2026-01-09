package com.rentify.rentify_api.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessExpireTime;
    private final long refreshExpireTime;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-expire-time}") long accessExpireTime,
        @Value("${jwt.refresh-token-expire-time}") long refreshExpireTime
    ){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireTime = accessExpireTime;
        this.refreshExpireTime = refreshExpireTime;
    }

    // Access Token 생성
    public String createAccessToken(Long userId){
        return createToken(userId, accessExpireTime);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId){
        return createToken(userId, refreshExpireTime);
    }

    private String createToken(Long userId, long expireTime){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setSubject(userId.toString())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.ES256)
                .compact();
    }
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // 유효성 검사
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
