package com.inseong.dallyrun.backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_OAUTH_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

    // Running Session
    RUNNING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "러닝 세션을 찾을 수 없습니다."),
    RUNNING_SESSION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 진행 중인 러닝 세션이 있습니다."),
    RUNNING_SESSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 종료된 러닝 세션입니다."),

    // Goal
    GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "목표를 찾을 수 없습니다."),

    // Badge
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "뱃지를 찾을 수 없습니다."),

    // Share
    SHARE_NOT_FOUND(HttpStatus.NOT_FOUND, "공유 링크를 찾을 수 없습니다."),

    // Common
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
