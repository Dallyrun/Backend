package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.config.ShareConfig;
import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
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
    private final StringRedisTemplate redisTemplate;
    private final ShareConfig shareConfig;

    public ShareServiceImpl(RunningSessionRepository runningSessionRepository,
                            StringRedisTemplate redisTemplate,
                            ShareConfig shareConfig) {
        this.runningSessionRepository = runningSessionRepository;
        this.redisTemplate = redisTemplate;
        this.shareConfig = shareConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public ShareDataResponse getShareData(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        return ShareDataResponse.of(session.getMember().getNickname(), session);
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

        RunningSession session = runningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUNNING_SESSION_NOT_FOUND));

        return ShareDataResponse.of(session.getMember().getNickname(), session);
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
