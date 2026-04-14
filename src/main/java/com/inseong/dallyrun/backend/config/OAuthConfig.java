package com.inseong.dallyrun.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record OAuthConfig(
        ProviderConfig kakao,
        ProviderConfig google
) {
    public record ProviderConfig(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
    }
}
