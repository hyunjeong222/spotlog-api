package com.spring.spotlog_api.domain.member;

import com.spring.spotlog_api.domain.member.dto.SignupRequest;
import com.spring.spotlog_api.domain.member.dto.SignupResponse;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 회원 생성 (기본 역할: CUSTOMER)
        Member member = Member.create(
                request.name(),
                request.email(),
                encodedPassword,
                MemberRole.CUSTOMER
        );

        memberRepository.save(member);

        return SignupResponse.from(member);
    }
}