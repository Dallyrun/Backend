package com.inseong.dallyrun.backend.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장소 추상화.
 *
 * <p>현재 구현체는 로컬 디스크({@link LocalFileStorage}). 추후 S3 등으로 교체될 수 있도록
 * 인터페이스로 분리되어 있다. URL 포맷은 구현체별로 다를 수 있으므로 {@link #delete(String)}는
 * 반드시 {@link #store(MultipartFile, String)}가 반환한 값 그대로를 받아야 한다.
 */
public interface FileStorage {

    /**
     * 파일을 저장하고 외부에서 접근 가능한 URL을 반환한다.
     *
     * @param file      업로드 대상 파일. content-type은 호출자가 사전에 검증해야 한다.
     * @param keyPrefix 저장소 내 논리 그룹 (예: {@code profile-images})
     * @return 공개 접근 URL
     */
    String store(MultipartFile file, String keyPrefix);

    /**
     * 이전에 저장된 파일을 삭제한다. 이미 삭제되었거나 존재하지 않으면 조용히 무시한다.
     *
     * @param url {@link #store} 가 반환했던 URL. 다른 베이스 URL이면 무시한다.
     */
    void delete(String url);
}
