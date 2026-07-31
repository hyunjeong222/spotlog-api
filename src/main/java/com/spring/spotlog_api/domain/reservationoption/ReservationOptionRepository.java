package com.spring.spotlog_api.domain.reservationoption;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationOptionRepository extends JpaRepository<ReservationOption, Long> {
    // 비관적 락: 이 row를 조회하는 순간 트랜잭션 종료까지 다른 요청은 대기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ReservationOption o where o.id = :id")
    Optional<ReservationOption> findByIdForUpdate(@Param("id") Long id);
}
