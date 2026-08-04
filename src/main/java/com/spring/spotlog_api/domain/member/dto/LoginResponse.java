package com.spring.spotlog_api.domain.member.dto;

import java.util.UUID;

public record LoginResponse (
    UUID memberId,
    String accessToken,
    String refreshToken
) {}