package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;
import com.inseong.dallyrun.backend.entity.RunningSession;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import com.inseong.dallyrun.backend.repository.RunningSessionRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class ShareServiceImpl implements ShareService {

    private static final String SHARE_PREFIX = "share:";
    private static final long SHARE_TTL_DAYS = 30;

    private final RunningSessionRepository runningSessionRepository;
    private final StringRedisTemplate redisTemplate;

    public ShareServiceImpl(RunningSessionRepository runningSessionRepository,
                            StringRedisTemplate redisTemplate) {
        this.runningSessionRepository = runningSessionRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ShareDataResponse getShareData(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);
        return ShareDataResponse.of(session.getMember().getNickname(), session);
    }

    @Override
    public ShareLinkResponse createShareLink(Long memberId, Long sessionId) {
        RunningSession session = getOwnedSession(memberId, sessionId);

        String shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        redisTemplate.opsForValue().set(
                SHARE_PREFIX + shareCode,
                session.getId().toString(),
                SHARE_TTL_DAYS,
                TimeUnit.DAYS
        );

        String shareUrl = "/api/shares/" + shareCode;
        return new ShareLinkResponse(shareCode, shareUrl);
    }

    @Override
    public ShareDataResponse getSharedData(String shareCode) {
        String sessionIdStr = redisTemplate.opsForValue().get(SHARE_PREFIX + shareCode);
        if (sessionIdStr == null) {
            throw new BusinessException(ErrorCode.SHARE_NOT_FOUND);
        }

        Long sessionId = Long.parseLong(sessionIdStr);
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
}
