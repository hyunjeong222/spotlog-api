package com.spring.spotlog_api.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    // 같은 옵션 + 날짜 + 시작시간 조합으로 이미 예약이 있는지 확인
    boolean existsByOption_IdAndReservationDateAndStartTimeAndStatus(
            UUID optionId, LocalDate reservationDate, LocalTime startTime, ReservationStatus status
    );

    // 조회
    List<Reservation> findByMember_IdOrderByReservationDateDescStartTimeDesc(UUID memberId);
}
