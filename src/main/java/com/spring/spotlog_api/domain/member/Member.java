package com.spring.spotlog_api.domain.member;

import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Member(String name, String email, String password, MemberRole role) {
        this.name = name.trim();
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member create(String name, String email,
                                String encodedPassword, MemberRole role) {
        return new Member(name, email, encodedPassword, role);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOwner() {
        return this.role == MemberRole.OWNER;
    }

    public void promoteToOwner() {
        if (this.role == MemberRole.OWNER) {
            throw new CustomException(ErrorCode.ALREADY_OWNER);
        }
        this.role = MemberRole.OWNER;
    }
}