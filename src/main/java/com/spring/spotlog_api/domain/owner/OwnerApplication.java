package com.spring.spotlog_api.domain.owner;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "owner_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "business_number", nullable = false, length = 20)
    private String businessNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApplicationStatus status;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    private OwnerApplication(Member member, String businessNumber) {
        this.member = member;
        this.businessNumber = businessNumber;
        this.status = ApplicationStatus.PENDING;
    }

    public static OwnerApplication create(Member member, String businessNumber) {
        return new OwnerApplication(member, businessNumber);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }
    }

    // 승인
    public void approve() {
        validatePending();
        this.status = ApplicationStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    // 거절
    public void reject(String reason) {
        validatePending();
        if (reason == null || reason.isBlank()) {
            throw new CustomException(ErrorCode.REJECTION_REASON_REQUIRED);
        }
        this.status = ApplicationStatus.REJECTED;
        this.reason = reason;
        this.processedAt = LocalDateTime.now();
    }

    // 취소
    public void cancel() {
        validatePending();
        this.status = ApplicationStatus.CANCELED;
        this.processedAt = LocalDateTime.now();
    }
}