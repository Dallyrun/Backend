package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import jakarta.persistence.*;

/**
 * 회원 엔티티.
 * OAuth 소셜 로그인으로만 가입하며, provider+providerId 조합으로 고유 식별한다.
 * RunningSession, Goal, MemberBadge와 1:N 관계를 갖는다.
 */
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"oauth_provider", "oauth_provider_id"})
})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = false, length = 255)
    private String oauthProviderId;

    protected Member() {
    }

    public Member(String email, String nickname, String profileImageUrl,
                  OAuthProvider oauthProvider, String oauthProviderId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public OAuthProvider getOauthProvider() {
        return oauthProvider;
    }

    public String getOauthProviderId() {
        return oauthProviderId;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
