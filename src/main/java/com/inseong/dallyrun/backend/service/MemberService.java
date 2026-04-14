package com.inseong.dallyrun.backend.service;

import com.inseong.dallyrun.backend.dto.request.MemberUpdateRequest;
import com.inseong.dallyrun.backend.dto.response.MemberResponse;

public interface MemberService {

    MemberResponse getProfile(Long memberId);

    MemberResponse updateProfile(Long memberId, MemberUpdateRequest request);

    void deleteMember(Long memberId);
}
