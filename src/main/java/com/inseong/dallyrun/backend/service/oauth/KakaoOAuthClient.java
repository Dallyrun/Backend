package com.inseong.dallyrun.backend.service.oauth;

import com.inseong.dallyrun.backend.config.OAuthConfig;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import com.inseong.dallyrun.backend.exception.BusinessException;
import com.inseong.dallyrun.backend.exception.ErrorCode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final OAuthConfig.ProviderConfig config;
    private final RestClient restClient;

    public KakaoOAuthClient(OAuthConfig oAuthConfig, RestClient.Builder restClientBuilder) {
        this.config = oAuthConfig.kakao();
        this.restClient = restClientBuilder.build();
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String authCode) {
        String accessToken = getAccessToken(authCode);
        return fetchUserInfo(accessToken);
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken(String authCode) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", config.clientId());
            params.add("client_secret", config.clientSecret());
            params.add("redirect_uri", config.redirectUri());
            params.add("code", authCode);

            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            return (String) response.get("access_token");
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE);
        }
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo fetchUserInfo(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            String id = String.valueOf(response.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.getOrDefault("email", "kakao_" + id + "@noreply.dallyrun.app");
            String nickname = (String) profile.getOrDefault("nickname", "카카오 사용자");
            String profileImageUrl = (String) profile.get("profile_image_url");

            return new OAuthUserInfo(OAuthProvider.KAKAO, id, email, nickname, profileImageUrl);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE);
        }
    }
}
