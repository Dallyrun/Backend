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
public class GoogleOAuthClient implements OAuthClient {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final OAuthConfig.ProviderConfig config;
    private final RestClient restClient;

    public GoogleOAuthClient(OAuthConfig oAuthConfig, RestClient.Builder restClientBuilder) {
        this.config = oAuthConfig.google();
        this.restClient = restClientBuilder.build();
    }

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuthUserInfo getUserInfo(String authCode) {
        String accessToken = getAccessToken(authCode);
        return fetchUserInfo(accessToken);
    }

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

    private OAuthUserInfo fetchUserInfo(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            String id = (String) response.get("id");
            String email = (String) response.getOrDefault("email", "google_" + id + "@noreply.dallyrun.app");
            String name = (String) response.getOrDefault("name", "Google 사용자");
            String picture = (String) response.get("picture");

            return new OAuthUserInfo(OAuthProvider.GOOGLE, id, email, name, picture);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.INVALID_OAUTH_CODE);
        }
    }
}
