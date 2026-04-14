package com.inseong.dallyrun.backend.security;

import com.inseong.dallyrun.backend.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig(
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-testing",
                1800000L,    // 30 minutes
                1209600000L  // 14 days
        );
        jwtTokenProvider = new JwtTokenProvider(config);
    }

    @Test
    void createAccessToken_andValidate() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void createRefreshToken_andValidate() {
        String token = jwtTokenProvider.createRefreshToken(1L);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getMemberIdFromToken() {
        Long memberId = 42L;
        String token = jwtTokenProvider.createAccessToken(memberId);

        assertEquals(memberId, jwtTokenProvider.getMemberIdFromToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_null_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtConfig shortLivedConfig = new JwtConfig(
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-testing",
                -1000L,  // already expired
                -1000L
        );
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedConfig);

        String token = shortLivedProvider.createAccessToken(1L);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void differentMemberIds_produceDifferentTokens() {
        String token1 = jwtTokenProvider.createAccessToken(1L);
        String token2 = jwtTokenProvider.createAccessToken(2L);

        assertNotEquals(token1, token2);
        assertEquals(1L, jwtTokenProvider.getMemberIdFromToken(token1));
        assertEquals(2L, jwtTokenProvider.getMemberIdFromToken(token2));
    }
}
