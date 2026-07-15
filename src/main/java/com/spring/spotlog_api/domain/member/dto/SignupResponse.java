package com.spring.spotlog_api.domain.member.dto;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRole;

public record SignupResponse (
    String name,
    String email,
    MemberRole role
) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
    }
}