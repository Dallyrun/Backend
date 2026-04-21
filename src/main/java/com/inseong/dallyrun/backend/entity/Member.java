package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

/**
 * 회원 엔티티.
 * 이메일/비밀번호로 가입하며, 이메일이 고유 식별자 역할을 한다.
 * 비밀번호는 BCrypt로 해시된 값만 저장한다.
 * RunningSession, Goal, MemberBadge와 1:N 관계를 갖는다.
 */
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    protected Member() {
    }

    public Member(String email, String passwordHash, String nickname, String profileImageUrl) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
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
