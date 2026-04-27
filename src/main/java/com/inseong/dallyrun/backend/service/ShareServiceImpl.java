package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.config.ShareConfig;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.MemberRepository;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ShareServiceImpl implements ShareService {

    private static final String SHARE_PREFIX = "share:";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHARE_CODE_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RunningSessionRepository runningSessionRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;
    private final ShareConfig shareConfig;

    public ShareServiceImpl(RunningSessionRepository runningSessionRepository,
                            MemberRepository memberRepository,
                            StringRedisTemplate redisTemplate,
                            ShareConfig shareConfig) {
        this.runningSessionRepository = runningSessionRepository;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
        this.shareConfig = shareConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public ShareDataResponse getShareData(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        Member member = loadActiveMember(session);
        return ShareDataResponse.of(member.getNickname(), session);
    }

    @Override
    public ShareLinkResponse createShareLink(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);

        String shareCode = generateShareCode();

        redisTemplate.opsForValue().set(
                SHARE_PREFIX + shareCode,
                session.getId().toString(),
                shareConfig.ttlDays(),
                TimeUnit.DAYS
        );

        String shareUrl = "/api/shares/" + shareCode;
        return new ShareLinkResponse(shareCode, shareUrl);
    }

    /**
     * 공개 공유 링크 조회 (인증 불필요).
     *
     * <p>탈퇴(soft delete)한 회원의 데이터가 외부에 노출되지 않도록,
     * 세션 소유 회원이 살아있는지 명시적으로 확인한다. 회원이 탈퇴 상태면
     * {@link ErrorCode#SHARE_NOT_FOUND} (404) 로 응답해 만료된 링크처럼 보이게 한다.
     * Redis 의 공유 키는 TTL 만료로 자연 정리됨.
     */
    @Override
    @Transactional(readOnly = true)
    public ShareDataResponse getSharedData(String shareCode) {
        String sessionIdStr = redisTemplate.opsForValue().get(SHARE_PREFIX + shareCode);
        if (sessionIdStr == null) {
            throw new BusinessException(ErrorCode.SHARE_NOT_FOUND);
        }

        // Redis 값이 손상되었거나 숫자 형식이 아닌 경우에도 일관된 404 응답을 반환한다.
        Long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SHARE_NOT_FOUND);
        }

        // 세션을 못 찾거나 회원이 탈퇴한 경우 모두 SHARE_NOT_FOUND 로 통일 (정보 노출 정책 일관성).
        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_NOT_FOUND));

        Member member = loadActiveMember(session);
        return ShareDataResponse.of(member.getNickname(), session);
    }

    private RunningSession getOwnedSession(Long memberId, Long sessionId) {
        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUNNING_SESSION_NOT_FOUND));
        if (!session.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return session;
    }

    /**
     * 세션의 소유 회원을 명시적으로 조회한다. {@code session.getMember()} lazy proxy 의
     * 초기화는 {@link com.inseong.dallyrun.backend.entity.Member} 의 {@code @SQLRestriction}
     * 으로 인해 탈퇴 회원에 대해선 {@code EntityNotFoundException} 을 던지므로,
     * FK ID 만 꺼내 {@link MemberRepository#findById} 로 안전하게 조회한다.
     * 탈퇴 회원이면 {@link ErrorCode#SHARE_NOT_FOUND}.
     */
    private Member loadActiveMember(RunningSession session) {
        Long memberId = session.getMember().getId(); // FK only — proxy 초기화 X
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHARE_NOT_FOUND));
    }

    /**
     * 16자리 랜덤 공유 코드를 생성한다.
     * SecureRandom으로 영문 대소문자+숫자(62종) 중 무작위 선택하여 URL-safe한 코드를 만든다.
     * 62^16 ≈ 4.7×10^28 조합으로 충돌 확률이 극히 낮다.
     */
    private String generateShareCode() {
        StringBuilder sb = new StringBuilder(SHARE_CODE_LENGTH);
        for (int i = 0; i < SHARE_CODE_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
