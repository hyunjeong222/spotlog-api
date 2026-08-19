package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.place.Place;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_id", columnDefinition = "BINARY(16)")
    private UUID id;

    // 소속 장소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, columnDefinition = "BINARY(16)")
    private Place place;

    // 옵션명 (예: "2인 테이블", "스터디룸 A")
    @Column(nullable = false)
    private String name;

    // 옵션 설명
    @Column(length = 500)
    private String description;

    // 정원 (한 슬롯에 최대 몇 명/팀까지 수용 가능한지)
    @Column(nullable = false)
    private Integer capacity;

    // 슬롯 단위 시간(분) — 예: 30분 단위 예약
    @Column(nullable = false)
    private Integer slotDurationMinutes;

    @Column(nullable = false)
    private LocalTime availableStartTime;

    @Column(nullable = false)
    private LocalTime availableEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationOptionStatus status;

    // 낙관적 락 실험용 버전 컬럼
    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static ReservationOption create(Place place, String name, String description,
                                           Integer capacity, Integer slotDurationMinutes,
                                           LocalTime availableStartTime, LocalTime availableEndTime) {
        ReservationOption option = new ReservationOption();
        option.place = place;
        option.name = name;
        option.description = description;
        option.capacity = capacity;
        option.slotDurationMinutes = slotDurationMinutes;
        option.availableStartTime = availableStartTime;
        option.availableEndTime = availableEndTime;
        option.status = ReservationOptionStatus.ACTIVE;
        return option;
    }

    public void update(String name, String description, Integer capacity, Integer slotDurationMinutes,
                       LocalTime availableStartTime, LocalTime availableEndTime) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (capacity != null) this.capacity = capacity;
        if (slotDurationMinutes != null) this.slotDurationMinutes = slotDurationMinutes;

        if (availableStartTime != null || availableEndTime != null) {
            LocalTime newStart = availableStartTime != null ? availableStartTime : this.availableStartTime;
            LocalTime newEnd = availableEndTime != null ? availableEndTime : this.availableEndTime;
            validateWithinPlaceHours(this.place, newStart, newEnd);
            this.availableStartTime = newStart;
            this.availableEndTime = newEnd;
        }
    }

    public void activate() {
        if (this.status == ReservationOptionStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_ACTIVE_OPTION);
        }
        this.status = ReservationOptionStatus.ACTIVE;
    }

    public void deactivate() {
        if (this.status == ReservationOptionStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_INACTIVE_OPTION);
        }
        this.status = ReservationOptionStatus.INACTIVE;
    }

    private static void validateWithinPlaceHours(Place place, LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_TIME_RANGE);
        }
        if (start.isBefore(place.getOpenTime()) || end.isAfter(place.getCloseTime())) {
            throw new CustomException(ErrorCode.OPTION_TIME_OUT_OF_PLACE_HOURS);
        }
    }
}