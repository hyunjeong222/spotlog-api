package com.spring.spotlog_api.domain.reservation.service;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.member.MemberRepository;
import com.spring.spotlog_api.domain.reservation.Reservation;
import com.spring.spotlog_api.domain.reservation.ReservationRepository;
import com.spring.spotlog_api.domain.reservation.ReservationStatus;
import com.spring.spotlog_api.domain.reservation.dto.ReservationCreateRequest;
import com.spring.spotlog_api.domain.reservation.dto.ReservationResponse;
import com.spring.spotlog_api.domain.reservationoption.ReservationOption;
import com.spring.spotlog_api.domain.reservationoption.ReservationOptionRepository;
import com.spring.spotlog_api.domain.reservationoption.ReservationOptionStatus;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PessimisticReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationOptionRepository optionRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationResponse reserve(UUID memberId, ReservationCreateRequest request) {
        // 1. 옵션 row에 비관적 락을 걸어서, 같은 옵션에 대한 동시 요청을 순차 처리
        ReservationOption option = optionRepository.findByIdForUpdate(request.optionId())
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (option.getStatus() == ReservationOptionStatus.INACTIVE) {
            throw new CustomException(ErrorCode.INACTIVE_OPTION);
        }

        // 2. 락을 잡은 상태에서 같은 슬롯 중복 예약 여부 확인
        boolean alreadyReserved = reservationRepository
                .existsByOption_IdAndReservationDateAndStartTimeAndStatus(
                        option.getId(), request.reservationDate(), request.startTime(), ReservationStatus.RESERVED
                );
        if (alreadyReserved) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Reservation reservation = Reservation.create(
                member, option, request.reservationDate(), request.startTime(), request.endTime(), request.quantity()
        );

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findMyReservations(UUID memberId) {
        return reservationRepository.findByMember_IdOrderByReservationDateDescStartTimeDesc(memberId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findOne(UUID memberId, UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_RESERVATION_PERMISSION);
        }
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancel(UUID memberId, UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancel(memberId);
    }
}