package com.spring.spotlog_api.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
    // 이메일 중복 체크
    boolean existsByEmail(String email);
}
