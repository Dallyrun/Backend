package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    MemberResponse getProfile(Long memberId);

    MemberResponse updateProfile(Long memberId, MemberUpdateRequest request);

    /**
     * 회원의 프로필 이미지를 업로드하여 교체한다. 이전 이미지가 있으면 스토리지에서 삭제한다.
     * MIME 타입은 이미지 계열 (jpeg/png/webp) 만 허용하며, 크기 제한은
     * {@code spring.servlet.multipart.max-file-size} 설정으로 강제된다.
     */
    MemberResponse uploadProfileImage(Long memberId, MultipartFile file);

    void deleteMember(Long memberId);
}
