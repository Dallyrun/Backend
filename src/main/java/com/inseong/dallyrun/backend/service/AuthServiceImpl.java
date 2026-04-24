package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.storage.FileStorage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String PROFILE_IMAGE_KEY_PREFIX = "profile-images";
    private static final Set<String> ALLOWED_IMAGE_MIMES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage fileStorage;

    public AuthServiceImpl(MemberRepository memberRepository,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate redisTemplate,
                           PasswordEncoder passwordEncoder,
                           FileStorage fileStorage) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.fileStorage = fileStorage;
    }

    /**
     * 이메일/비밀번호 회원가입.
     *
     * <p>이메일·닉네임 중복 시 409. 비밀번호는 BCrypt로 해시하여 저장한다.
     * 프로필 이미지는 필수이며 허용된 MIME(jpeg/png/webp)만 허용한다.
     * 이미지 업로드 후 DB 저장이 실패하면 저장된 파일을 보상 삭제하여 고아 파일을 방지한다.
     * 가입 성공 시 즉시 JWT access/refresh 토큰을 발급하여 로그인 상태로 응답한다.
     */
    @Override
    public TokenResponse signup(SignupRequest request, MultipartFile profileImage) {
        validateProfileImage(profileImage);

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        String imageUrl = fileStorage.store(profileImage, PROFILE_IMAGE_KEY_PREFIX);
        try {
            String passwordHash = passwordEncoder.encode(request.password());
            Member member = memberRepository.save(new Member(
                    request.email(),
                    passwordHash,
                    request.nickname(),
                    imageUrl,
                    request.ageBracket(),
                    request.gender()
            ));
            return issueTokens(member.getId());
        } catch (RuntimeException e) {
            fileStorage.delete(imageUrl);
            throw e;
        }
    }

    private void validateProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_REQUIRED);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIMES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 이메일/비밀번호 로그인.
     *
     * <p>회원 존재 여부와 비밀번호 일치 여부를 구분하지 않고 동일한 INVALID_CREDENTIALS 응답을
     * 반환하여 사용자 열거(enumeration) 공격을 방지한다.
     */
    @Override
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(member.getId());
    }

    /**
     * Refresh Token Rotation 방식으로 토큰을 갱신한다.
     *
     * <p>검증 흐름:
     * 1) 토큰 서명·만료 유효성 검사
     * 2) Redis에 저장된 토큰과 일치 여부 확인 (탈취된 토큰 재사용 방지)
     * 3) 새 access/refresh 토큰 쌍 발급 → Redis의 기존 refresh 토큰을 새 토큰으로 교체
     */
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + memberId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return issueTokens(memberId);
    }

    @Override
    public void logout(Long memberId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
    }

    private TokenResponse issueTokens(Long memberId) {
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + memberId,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiry(),
                TimeUnit.MILLISECONDS
        );

        return new TokenResponse(accessToken, refreshToken);
    }
}
