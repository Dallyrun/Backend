package com.inseong.dallyrun.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * CORS 정책 설정.
     * /api/** 경로에 대해 환경변수로 지정된 오리진만 허용하며,
     * 인증 쿠키 전송(allowCredentials)을 위해 와일드카드(*) 오리진은 사용하지 않는다.
     * preflight 캐시(maxAge)는 1시간으로 설정하여 불필요한 OPTIONS 요청을 줄인다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
