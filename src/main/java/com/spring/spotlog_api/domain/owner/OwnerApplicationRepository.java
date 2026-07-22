package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OwnerApplicationRepository extends JpaRepository<OwnerApplication, UUID> {
    // 특정 회원의 대기 중인 신청이 있는지 확인
    boolean existsByMemberAndStatus(Member member, ApplicationStatus status);

    // PENDING, APPROVED 사업자 신청 차단
    boolean existsByBusinessNumberAndStatusIn(
            String businessNumber, List<ApplicationStatus> statuses);

    // 회원용 - 본인 신청 목록 조회 (페이징)
    Page<OwnerApplication> findByMember(Member member, Pageable pageable);

    // 회원용 - 본인 신청 상세 조회
    Optional<OwnerApplication> findByIdAndMember(UUID id, Member member);

    // 관리자용 - 상태별 목록 조회 (페이징) / status null이면 전체 조회는 Service에서 처리
    Page<OwnerApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
