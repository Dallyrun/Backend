package com.inseong.dallyrun.backend.storage;

import com.inseong.dallyrun.backend.config.StorageProperties;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage storage;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8080";
        storage = new LocalFileStorage(new StorageProperties(
                new StorageProperties.Local(tempDir.toString()),
                baseUrl
        ));
    }

    @Test
    void store_jpeg_returnsUrlAndWritesFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "original.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url = storage.store(file, "profile-images");

        assertTrue(url.startsWith(baseUrl + "/uploads/profile-images/"));
        assertTrue(url.endsWith(".jpg"));

        // 실제 파일이 저장되었는지 확인
        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path saved = tempDir.resolve("profile-images").resolve(filename);
        assertTrue(Files.exists(saved));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(saved));
    }

    @Test
    void store_png_usesCorrectExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.png", "image/png", new byte[]{9});
        String url = storage.store(file, "profile-images");
        assertTrue(url.endsWith(".png"));
    }

    @Test
    void store_webp_usesCorrectExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.webp", "image/webp", new byte[]{9});
        String url = storage.store(file, "profile-images");
        assertTrue(url.endsWith(".webp"));
    }

    @Test
    void store_uppercaseMime_isAccepted() {
        // 브라우저/클라이언트가 대소문자 다르게 보낼 수 있음 — 소문자 정규화 확인
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "IMAGE/JPEG", new byte[]{1});
        String url = storage.store(file, "profile-images");
        assertTrue(url.endsWith(".jpg"));
    }

    @Test
    void store_unsupportedMime_throwsInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.gif", "image/gif", new byte[]{1});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.store(file, "profile-images"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void store_nullMime_throwsInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x", null, new byte[]{1});
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.store(file, "profile-images"));
        assertEquals(ErrorCode.INVALID_FILE_TYPE, ex.getErrorCode());
    }

    @Test
    void store_pathTraversalKeyPrefix_throwsUploadFailed() {
        // keyPrefix 에 "../" 가 섞여 루트 밖으로 탈출 시도하면 차단되어야 한다
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{1});
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.store(file, "../outside"));
        assertEquals(ErrorCode.FILE_UPLOAD_FAILED, ex.getErrorCode());
    }

    @Test
    void delete_removesStoredFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{1});
        String url = storage.store(file, "profile-images");

        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path saved = tempDir.resolve("profile-images").resolve(filename);
        assertTrue(Files.exists(saved));

        storage.delete(url);

        assertFalse(Files.exists(saved));
    }

    @Test
    void delete_nonexistentUrl_doesNotThrow() {
        assertDoesNotThrow(() -> storage.delete(baseUrl + "/uploads/profile-images/ghost.jpg"));
    }

    @Test
    void delete_null_doesNotThrow() {
        assertDoesNotThrow(() -> storage.delete(null));
    }

    @Test
    void delete_externalUrl_isIgnored() throws Exception {
        // 외부(다른 베이스) URL은 삭제 시도하지 않아 파일시스템에 영향을 주지 않아야 한다.
        // 사전에 디렉토리 생성해두고, 외부 URL 삭제 호출 후에도 존재하는지 확인.
        Files.createDirectories(tempDir.resolve("profile-images"));
        Path sentinel = Files.writeString(tempDir.resolve("profile-images").resolve("keep.jpg"), "x");

        storage.delete("https://other.example.com/uploads/profile-images/keep.jpg");

        assertTrue(Files.exists(sentinel));
    }

    @Test
    void store_multipleCalls_generateUniqueFilenames() {
        MockMultipartFile a = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1});
        MockMultipartFile b = new MockMultipartFile("file", "b.jpg", "image/jpeg", new byte[]{2});

        String urlA = storage.store(a, "profile-images");
        String urlB = storage.store(b, "profile-images");

        // UUID 기반 파일명이므로 동일 키프리픽스에 저장해도 충돌하지 않음
        assertNotEquals(urlA, urlB);

        long fileCount = Stream.of(urlA, urlB).distinct().count();
        assertEquals(2, fileCount);
    }
}
