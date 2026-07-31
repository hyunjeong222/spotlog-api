package com.spring.spotlog_api.domain.reservationoption;

import com.spring.spotlog_api.domain.place.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    // 소속 장소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // 옵션명 (예: "2인 테이블", "스터디룸 A")
    @Column(nullable = false)
    private String name;

    // 정원 (한 슬롯에 최대 몇 명/팀까지 수용 가능한지)
    @Column(nullable = false)
    private Integer capacity;

    // 슬롯 단위 시간(분) — 예: 30분 단위 예약
    @Column(nullable = false)
    private Integer slotDurationMinutes;

    // 낙관적 락 실험용 버전 컬럼
    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static ReservationOption create(Place place, String name, Integer capacity, Integer slotDurationMinutes) {
        ReservationOption option = new ReservationOption();
        option.place = place;
        option.name = name;
        option.capacity = capacity;
        option.slotDurationMinutes = slotDurationMinutes;
        return option;
    }
}