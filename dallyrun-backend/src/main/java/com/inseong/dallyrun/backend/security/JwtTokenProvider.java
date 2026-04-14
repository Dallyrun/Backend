package com.inseong.dallyrun.backend.security;

import com.inseong.dallyrun.backend.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
