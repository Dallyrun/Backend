package com.inseong.dallyrun.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 에러 코드 정의.
 * 도메인별로 그룹화하여 HTTP 상태 코드와 사용자 메시지를 매핑한다.
 */
public enum ErrorCode {

    // Auth — 인증/인가 관련 오류
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_OAUTH_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),

    // Member — 회원 관련 오류
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // Running Session — 러닝 세션 관련 오류
    RUNNING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "러닝 세션을 찾을 수 없습니다."),
    RUNNING_SESSION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 진행 중인 러닝 세션이 있습니다."),
    RUNNING_SESSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 종료된 러닝 세션입니다."),

    // Goal — 목표 관련 오류
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "목표를 찾을 수 없습니다."),

    // Badge — 배지 관련 오류
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "뱃지를 찾을 수 없습니다."),

    // Share — 공유 링크 관련 오류
    SHARE_NOT_FOUND(HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다."),

    // Common — 공통 오류
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
