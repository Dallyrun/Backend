package com.inseong.dallyrun.backend.storage;

import com.inseong.dallyrun.backend.config.StorageProperties;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * 로컬 디스크 기반 파일 스토리지 구현체.
 *
 * <p>파일은 {@code {rootPath}/{keyPrefix}/{UUID}.{ext}} 로 저장되며, 공개 URL은
 * {@code {baseUrl}/uploads/{keyPrefix}/{UUID}.{ext}} 형식이다. 정적 서빙은
 * {@code WebConfig} 에서 {@code /uploads/**} 핸들러로 매핑한다.
 *
 * <p>보안 고려:
 * <ul>
 *   <li>사용자 제공 파일명을 그대로 사용하지 않고 UUID로 생성 (경로 주입/덮어쓰기 방지)</li>
 *   <li>저장 경로는 {@code rootPath} 하위로 제한 (경로 순회 방지)</li>
 *   <li>확장자는 MIME 타입 기반 매핑으로 결정 (클라이언트 제공 확장자 무시)</li>
 * </ul>
 */
@Component
public class LocalFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorage.class);

    private static final String URL_PATH_PREFIX = "/uploads/";

    /** 상위 서비스에서 MIME 검증을 통과한 타입만 이 맵에 등록되어 있어야 한다. */
    private static final Map<String, String> EXTENSION_BY_MIME = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path rootPath;
    private final String baseUrl;

    public LocalFileStorage(StorageProperties properties) {
        this.rootPath = Path.of(properties.local().path()).toAbsolutePath().normalize();
        String configured = properties.baseUrl();
        this.baseUrl = configured.endsWith("/")
                ? configured.substring(0, configured.length() - 1)
                : configured;
    }

    @Override
    public String store(MultipartFile file, String keyPrefix) {
        String mime = file.getContentType();
        String extension = EXTENSION_BY_MIME.get(mime == null ? "" : mime.toLowerCase());
        if (extension == null) {
            // 서비스 계층 검증을 통과한 MIME만 이 시점에 도달해야 하므로, 여기 걸리면 설정 오류.
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        String filename = UUID.randomUUID() + "." + extension;
        Path targetDir = rootPath.resolve(keyPrefix).normalize();
        // keyPrefix 에 "../" 등이 섞여 rootPath 밖으로 탈출하지 않도록 방어
        if (!targetDir.startsWith(rootPath)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(filename);
            file.transferTo(targetPath);
        } catch (IOException e) {
            log.error("Failed to store file to {}", targetDir, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return baseUrl + URL_PATH_PREFIX + keyPrefix + "/" + filename;
    }

    @Override
    public void delete(String url) {
        if (url == null) {
            return;
        }
        String urlBaseAndPrefix = baseUrl + URL_PATH_PREFIX;
        if (!url.startsWith(urlBaseAndPrefix)) {
            // 다른 베이스에서 발급한 URL (이전 스토리지/외부 이미지 등)은 삭제 시도하지 않음
            return;
        }

        String relative = url.substring(urlBaseAndPrefix.length());
        Path target = rootPath.resolve(relative).normalize();
        if (!target.startsWith(rootPath)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // 삭제 실패는 치명적이지 않음 — 고아 파일은 운영 관점에서 정리.
            log.warn("Failed to delete stored file {}", target, e);
        }
    }
}
