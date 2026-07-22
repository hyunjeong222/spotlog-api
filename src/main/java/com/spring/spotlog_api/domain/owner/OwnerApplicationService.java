package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRepository;
import com.spring.spotlog_api.domain.owner.dto.OwnerApplicationRequest;
import com.spring.spotlog_api.domain.owner.dto.OwnerApplicationResponse;
import com.spring.spotlog_api.global.common.PageResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerApplicationService {
    private final OwnerApplicationRepository ownerApplicationRepository;
    private final MemberRepository memberRepository;

    // 사업자 전환 신청
    @Transactional
    public OwnerApplicationResponse apply(String memberId, OwnerApplicationRequest request) {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 사업자인지 확인
        if (member.isOwner()) {
            throw new CustomException(ErrorCode.ALREADY_OWNER);
        }

        // 이미 대기 중인 신청이 있는지 확인
        if (ownerApplicationRepository.existsByMemberAndStatus(member, ApplicationStatus.PENDING)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        // PENDING + APPROVED 모두 차단
        if (ownerApplicationRepository.existsByBusinessNumberAndStatusIn(
                request.businessNumber(),
                List.of(ApplicationStatus.PENDING, ApplicationStatus.APPROVED))) {
            throw new CustomException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        OwnerApplication application = OwnerApplication.create(member, request.businessNumber());

        try {
            ownerApplicationRepository.save(application);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        return OwnerApplicationResponse.from(application);
    }

    // 신청 취소
    @Transactional
    public void cancel(String memberId, UUID applicationId) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getMember().getId().equals(UUID.fromString(memberId))) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        application.cancel();
    }

    // 본인 신청 목록 조회
    public PageResponse<OwnerApplicationResponse> getMyApplications(
            String memberId, Pageable pageable) {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Page<OwnerApplicationResponse> page = ownerApplicationRepository
                .findByMember(member, pageable)
                .map(OwnerApplicationResponse::from);

        return PageResponse.from(page);
    }

    // 본인 신청 상세 조회
    public OwnerApplicationResponse getMyApplication(String memberId, UUID applicationId) {
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        OwnerApplication application = ownerApplicationRepository
                .findByIdAndMember(applicationId, member)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        return OwnerApplicationResponse.from(application);
    }

    // 관리자용 신청 목록 조회 (페이징)
    public PageResponse<OwnerApplicationResponse> getApplications(
            ApplicationStatus status, Pageable pageable) {
        Page<OwnerApplication> page;

        // status 없으면 전체 조회, 있으면 상태별 필터링
        if (status != null) {
            page = ownerApplicationRepository.findByStatus(status, pageable);
        } else {
            page = ownerApplicationRepository.findAll(pageable);
        }

        return PageResponse.from(page.map(OwnerApplicationResponse::from));
    }

    // 관리자용 상세 조회
    public OwnerApplicationResponse getApplication(UUID applicationId) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        return OwnerApplicationResponse.from(application);
    }

    // 승인 (관리자)
    @Transactional
    public void approve(UUID applicationId) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        application.approve();
        application.getMember().promoteToOwner();
    }

    // 거절 (관리자)
    @Transactional
    public void reject(UUID applicationId, String reason) {
        OwnerApplication application = ownerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        application.reject(reason);
    }
}