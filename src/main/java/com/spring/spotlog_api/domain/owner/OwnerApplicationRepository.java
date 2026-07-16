package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OwnerApplicationRepository extends JpaRepository<OwnerApplication, UUID> {
    // 특정 회원의 대기 중인 신청이 있는지 확인
    boolean existsByMemberAndStatus(Member member, ApplicationStatus status);

    // 대기 중인 신청 목록 조회 (관리자용)
    List<OwnerApplication> findByStatus(ApplicationStatus status);
}
