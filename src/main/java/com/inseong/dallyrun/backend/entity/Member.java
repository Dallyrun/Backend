package com.inseong.dallyrun.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 회원 엔티티.
 * 이메일/비밀번호로 가입하며, 이메일과 닉네임이 각각 고유하다.
 * 비밀번호는 BCrypt로 해시된 값만 저장한다.
 * RunningSession, Goal, MemberBadge와 1:N 관계를 갖는다.
 *
 * <p>Soft delete: {@code deleted_at} 컬럼이 채워진 회원은 모든 SELECT 쿼리에서
 * 자동 제외된다 ({@link SQLRestriction}). 행 자체는 DB 에 유지되어
 * 자식 엔티티(러닝 세션 등)의 FK 무결성이 깨지지 않는다.
 */
@SQLRestriction("deleted_at IS NULL")
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 회원을 soft delete 한다. {@code deletedAt} 만 채우고 행은 유지한다.
     * 이후 모든 조회 쿼리는 {@link SQLRestriction} 에 의해 자동 제외된다.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
