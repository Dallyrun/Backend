package com.inseong.dallyrun.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI 3.0) 문서 설정.
 * 모든 API 엔드포인트에 JWT Bearer 인증 스키마를 전역 적용하여,
 * Swagger UI에서 "Authorize" 버튼으로 토큰을 입력하면 인증이 필요한 API를 테스트할 수 있다.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dallyrun API")
                        .description("런닝 트래킹 앱 백엔드 API")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .bearerFormat("JWT")
                                        .scheme("bearer")));
    }
}
