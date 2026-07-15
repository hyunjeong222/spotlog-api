package com.spring.spotlog_api.domain.member;

import com.spring.spotlog_api.domain.member.dto.*;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import com.spring.spotlog_api.global.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

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

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        // 토큰 생성
        String memberId = member.getId().toString();
        String accessToken = jwtUtil.generateAccessToken(memberId, member.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(memberId);

        // Redis에 Refresh Token 저장 (TTL 설정)
        redisTemplate.opsForValue().set(
                "refresh:" + memberId,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급
    public ReissueResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        // Refresh Token 유효성 검증
        jwtUtil.validateToken(refreshToken);

        // memberId 추출
        String memberId = jwtUtil.getMemberId(refreshToken);

        // Redis에서 저장된 Refresh Token 조회
        String savedToken = redisTemplate.opsForValue().get("refresh:" + memberId);

        // Redis에 토큰이 없으면 (로그아웃 or 만료)
        if (savedToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 요청 토큰과 Redis 저장 토큰 일치 여부 확인
        if (!savedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 회원 조회
        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 새 Access Token 발급
        String newAccessToken = jwtUtil.generateAccessToken(memberId, member.getRole().name());

        return new ReissueResponse(newAccessToken);
    }

    // 로그아웃
    public void logout(String accessToken) {
        // Access Token에서 memberId 추출
        String memberId = jwtUtil.getMemberId(accessToken);

        // Redis에서 Refresh Token 삭제
        redisTemplate.delete("refresh:" + memberId);

        // Access Token 블랙리스트 등록 (남은 만료시간만큼 TTL)
        long expiration = jwtUtil.getExpiration(accessToken);
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "blacklist:" + accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }
    }
}