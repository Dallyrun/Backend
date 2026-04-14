package com.inseong.dallyrun.backend.service.oauth;

import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;

public interface OAuthClient {

    OAuthProvider getProvider();

    OAuthUserInfo getUserInfo(String authCode);
}
