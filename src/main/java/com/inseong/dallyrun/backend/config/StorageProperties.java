package com.inseong.dallyrun.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 파일 스토리지 설정.
 *
 * <p>현재는 로컬 디스크 기반. 추후 S3 등으로 교체할 때는 {@code storage.local} 블록을 제거하고
 * 해당 구현체에 맞는 설정을 추가하면 된다.
 *
 * @param local   로컬 스토리지 설정 (저장 루트 경로)
 * @param baseUrl 외부에 노출할 기본 URL (예: {@code http://localhost:8080}) — 저장된 파일 URL 조립에 사용
 */
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        Local local,
        String baseUrl
) {
    public record Local(String path) {
    }
}
