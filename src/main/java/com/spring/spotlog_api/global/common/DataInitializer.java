package com.spring.spotlog_api.global.common;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRepository;
import com.spring.spotlog_api.domain.member.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!memberRepository.existsByEmail(adminEmail)) {
            memberRepository.save(Member.create(
                    "관리자",
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    MemberRole.ADMIN
            ));
            log.info("ADMIN 계정이 생성되었습니다.");
        } else {
            log.info("ADMIN 계정이 이미 존재합니다.");
        }
    }
}