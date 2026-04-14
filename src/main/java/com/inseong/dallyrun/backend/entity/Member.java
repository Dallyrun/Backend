package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import jakarta.persistence.*;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"oauth_provider", "oauth_provider_id"})
})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = false)
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
