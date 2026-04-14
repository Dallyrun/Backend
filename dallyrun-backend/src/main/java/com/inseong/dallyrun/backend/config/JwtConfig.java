package com.inseong.dallyrun.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(
        String secret,
        long accessTokenExpiry,
        long refreshTokenExpiry
) {
}
