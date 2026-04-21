package com.inseong.dallyrun.backend.security;

import com.inseong.dallyrun.backend.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String passwordHash;

    public CustomUserDetails(Member member) {
        this.memberId = member.getId();
        this.email = member.getEmail();
        this.passwordHash = member.getPasswordHash();
    }

    /** JWT 필터 등 패스워드 검증이 불필요한 경로에서 사용하는 경량 생성자. */
    public CustomUserDetails(Long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
        this.passwordHash = null;
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
