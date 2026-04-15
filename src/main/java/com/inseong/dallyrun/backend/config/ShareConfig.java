package com.inseong.dallyrun.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "share")
public record ShareConfig(int ttlDays) {
}
