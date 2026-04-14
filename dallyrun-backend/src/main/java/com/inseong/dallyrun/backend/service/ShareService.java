package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.response.ShareDataResponse;
import com.inseong.dallyrun.backend.dto.response.ShareLinkResponse;

public interface ShareService {

    ShareDataResponse getShareData(Long memberId, Long sessionId);

    ShareLinkResponse createShareLink(Long memberId, Long sessionId);

    ShareDataResponse getSharedData(String shareCode);
}
