package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.TokenResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.security.JwtTokenProvider;
import com.inseong.dallyrun.backend.service.oauth.OAuthClient;
import com.inseong.dallyrun.backend.service.oauth.OAuthUserInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    private final Map<OAuthProvider, OAuthClient> oauthClients;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    public AuthServiceImpl(List<OAuthClient> oauthClients,
                           MemberRepository memberRepository,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate redisTemplate) {
        this.oauthClients = oauthClients.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, Function.identity()));
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public TokenResponse oauthLogin(OAuthProvider provider, String authCode) {
        OAuthClient client = oauthClients.get(provider);
        if (client == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        OAuthUserInfo userInfo = client.getUserInfo(authCode);

        Member member = memberRepository
                .findByOauthProviderAndOauthProviderId(provider, userInfo.providerId())
                .orElseGet(() -> {
                    try {
                        return memberRepository.save(new Member(
                                userInfo.email(),
                                userInfo.nickname(),
                                userInfo.profileImageUrl(),
                                userInfo.provider(),
                                userInfo.providerId()
                        ));
                    } catch (DataIntegrityViolationException e) {
                        return memberRepository
                                .findByOauthProviderAndOauthProviderId(provider, userInfo.providerId())
                                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
                    }
                });

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
