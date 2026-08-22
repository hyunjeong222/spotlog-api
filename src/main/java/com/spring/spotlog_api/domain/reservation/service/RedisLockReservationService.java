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
import com.spring.spotlog_api.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisLockReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationOptionRepository optionRepository;
    private final MemberRepository memberRepository;

    @DistributedLock(key = "'reservation:' + #request.optionId() + ':' + #request.reservationDate() + ':' + #request.startTime()")
    public ReservationResponse reserve(UUID memberId, ReservationCreateRequest request) {
        // 비관적 락(findByIdForUpdate)이 아니라 일반 조회 — DB 락을 걸지 않음
        ReservationOption option = optionRepository.findById(request.optionId())
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));

        if (option.getStatus() == ReservationOptionStatus.INACTIVE) {
            throw new CustomException(ErrorCode.INACTIVE_OPTION);
        }

        validateSlotAlignment(option, request.startTime(), request.endTime());

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

        // DB Unique Constraint가 최후 방어선 — Redis 락이 뚫려도 여기서 막힘
        try {
            return ReservationResponse.from(reservationRepository.save(reservation));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED);
        }
    }

    private void validateSlotAlignment(ReservationOption option, LocalTime startTime, LocalTime endTime) {
        LocalTime optionStart = option.getAvailableStartTime();
        LocalTime optionEnd = option.getAvailableEndTime();
        int duration = option.getSlotDurationMinutes();

        long minutesFromStart = Duration.between(optionStart, startTime).toMinutes();

        boolean isAligned = minutesFromStart >= 0
                && minutesFromStart % duration == 0
                && startTime.plusMinutes(duration).equals(endTime)
                && !startTime.isBefore(optionStart)
                && !endTime.isAfter(optionEnd);

        if (!isAligned) {
            throw new CustomException(ErrorCode.INVALID_SLOT_TIME);
        }
    }
}