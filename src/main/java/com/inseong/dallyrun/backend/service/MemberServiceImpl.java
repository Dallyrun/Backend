package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.storage.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private static final Set<String> ALLOWED_IMAGE_MIMES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final String PROFILE_IMAGE_KEY_PREFIX = "profile-images";

    private final MemberRepository memberRepository;
    private final FileStorage fileStorage;

    public MemberServiceImpl(MemberRepository memberRepository, FileStorage fileStorage) {
        this.memberRepository = memberRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Override
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfile(request.nickname(), request.profileImageUrl());
        return MemberResponse.from(member);
    }

    /**
     * 프로필 이미지 업로드:
     * 1) 파일 존재·MIME 검증 (jpeg/png/webp만 허용)
     * 2) 회원 조회
     * 3) 스토리지 저장 → 새 URL 획득
     * 4) 이전 URL이 있으면 스토리지에서 삭제 (저장 성공 후에만 삭제하여 실패 시 기존 이미지 보존)
     */
    @Override
    public MemberResponse uploadProfileImage(Long memberId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIMES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String previousUrl = member.getProfileImageUrl();
        String newUrl = fileStorage.store(file, PROFILE_IMAGE_KEY_PREFIX);
        member.updateProfile(null, newUrl);

        if (previousUrl != null) {
            fileStorage.delete(previousUrl);
        }

        return MemberResponse.from(member);
    }

    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }
}
