package com.spring.spotlog_api.domain.member.dto;

public record LoginResponse (
    String accessToken,
    String refreshToken
) {}