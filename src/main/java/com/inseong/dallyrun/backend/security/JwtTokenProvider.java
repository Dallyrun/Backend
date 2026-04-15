package com.inseong.dallyrun.backend.security;

import com.inseong.dallyrun.backend.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당한다.
 *
 * <p>토큰 구조: subject에 memberId를 저장하고, HMAC-SHA 알고리즘으로 서명한다.
 * Access Token과 Refresh Token은 동일한 구조이며, 만료 시간만 다르다.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.key = Keys.hmacShaKeyFor(jwtConfig.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = jwtConfig.accessTokenExpiry();
        this.refreshTokenExpiry = jwtConfig.refreshTokenExpiry();
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessTokenExpiry);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshTokenExpiry);
    }

    private String createToken(Long memberId, long expiry) {
        Date now = new Date();
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry))
                .signWith(key)
                .compact();
    }

    public Long getMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰의 서명과 만료 시간을 검증한다.
     * 서명 불일치, 만료, 형식 오류 등 모든 예외를 false로 처리한다.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }
}
