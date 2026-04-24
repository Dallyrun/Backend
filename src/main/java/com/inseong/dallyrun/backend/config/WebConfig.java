package com.inseong.dallyrun.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final Path uploadRoot;

    public WebConfig(@Value("${cors.allowed-origins}") String allowedOrigins,
                     @Value("${storage.local.path}") String uploadLocalPath) {
        this.allowedOrigins = allowedOrigins.split(",");
        this.uploadRoot = Path.of(uploadLocalPath).toAbsolutePath().normalize();
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
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 로컬 스토리지에 저장된 업로드 파일을 정적으로 서빙한다.
     * {@code /uploads/**} 요청은 {@code storage.local.path} 디렉토리에서 파일을 찾아 응답한다.
     * 추후 S3 등 외부 스토리지로 교체되면 이 핸들러는 제거된다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot + "/");
    }
}
