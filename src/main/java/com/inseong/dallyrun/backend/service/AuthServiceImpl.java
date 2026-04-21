package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.LoginRequest;
import com.inseong.dallyrun.backend.dto.request.SignupRequest;
import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(MemberRepository memberRepository,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate redisTemplate,
                           PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 이메일/비밀번호 회원가입.
     *
     * <p>이메일 중복 시 EMAIL_ALREADY_EXISTS. 비밀번호는 BCrypt로 해시하여 저장한다.
     * 가입 성공 시 즉시 JWT access/refresh 토큰을 발급하여 로그인 상태로 응답한다.
     */
    @Override
    public TokenResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Member member = memberRepository.save(new Member(
                request.email(),
                passwordHash,
                request.nickname(),
                null
        ));

        return issueTokens(member.getId());
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
