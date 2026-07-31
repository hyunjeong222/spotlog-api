package com.spring.spotlog_api.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // JWT
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호를 확인해 주세요."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    ALREADY_OWNER(HttpStatus.BAD_REQUEST, "이미 사업자 회원입니다."),

    // 사업자 신청
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 처리 중인 신청이 있습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신청입니다."),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 신청입니다."),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "거절 사유는 필수입니다."),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 장소
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장소입니다."),
    ALREADY_ACTIVE_PLACE(HttpStatus.BAD_REQUEST, "이미 활성화된 장소입니다."),
    ALREADY_INACTIVE_PLACE(HttpStatus.BAD_REQUEST, "이미 비활성화된 장소입니다."),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "영업 종료 시간은 시작 시간보다 늦어야 합니다."),
    INVALID_LOCATION(HttpStatus.BAD_REQUEST, "유효하지 않은 위치 정보입니다."),
    NO_PLACE_PERMISSION(HttpStatus.FORBIDDEN, "장소에 대한 권한이 없습니다."),
    DUPLICATE_PLACE(HttpStatus.CONFLICT, "이미 등록된 장소입니다."),

    // 예약
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 옵션입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),
    ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 시간입니다."),
    INVALID_RESERVATION_TIME_RANGE(HttpStatus.BAD_REQUEST, "예약 종료 시간은 시작 시간보다 늦어야 합니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "잘못된 상태 변경입니다."),
    NO_RESERVATION_PERMISSION(HttpStatus.FORBIDDEN, "예약에 대한 권한이 없습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "예약 요청이 몰려 처리에 실패했습니다. 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;
}