package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.storage.FileStorage;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public MemberServiceImpl(MemberRepository memberRepository,
                             FileStorage fileStorage,
                             PasswordEncoder passwordEncoder,
                             AuthService authService) {
        this.memberRepository = memberRepository;
        this.fileStorage = fileStorage;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
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

    /**
     * 회원 soft delete.
     * 1) 회원 조회 (이미 탈퇴된 회원은 {@code @SQLRestriction} 으로 자동 제외 → MEMBER_NOT_FOUND)
     * 2) 비밀번호 재확인 (불일치 시 INVALID_CREDENTIALS)
     * 3) {@code deletedAt} 채워서 dirty checking 으로 UPDATE
     * 4) Redis 의 refresh token 즉시 폐기
     */
    @Override
    public void deleteMember(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        member.softDelete();
        authService.logout(memberId);
    }
}
