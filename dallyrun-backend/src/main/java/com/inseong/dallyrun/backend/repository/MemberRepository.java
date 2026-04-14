package com.inseong.dallyrun.backend.repository;

import com.inseong.dallyrun.backend.entity.Member;
import com.inseong.dallyrun.backend.entity.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthProviderAndOauthProviderId(OAuthProvider provider, String providerId);

    Optional<Member> findByEmail(String email);
}
