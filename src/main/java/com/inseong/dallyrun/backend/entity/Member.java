package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;

/**
 * 회원 엔티티.
 * 이메일/비밀번호로 가입하며, 이메일과 닉네임이 각각 고유하다.
 * 비밀번호는 BCrypt로 해시된 값만 저장한다.
 * RunningSession, Goal, MemberBadge와 1:N 관계를 갖는다.
 */
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nickname")
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

    @Column(nullable = false, length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgeBracket ageBracket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    protected Member() {
    }

    public Member(String email, String passwordHash, String nickname,
                  String profileImageUrl, AgeBracket ageBracket, Gender gender) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.ageBracket = ageBracket;
        this.gender = gender;
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

    public AgeBracket getAgeBracket() {
        return ageBracket;
    }

    public Gender getGender() {
        return gender;
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
