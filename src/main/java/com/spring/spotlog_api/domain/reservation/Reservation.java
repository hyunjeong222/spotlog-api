package com.spring.spotlog_api.domain.reservation;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.reservationoption.ReservationOption;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_option_date_start_time",
                        columnNames = {"option_id", "reservation_date", "start_time"}
                )
        }
)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, columnDefinition = "BINARY(16)")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false, columnDefinition = "BINARY(16)")
    private ReservationOption option;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static Reservation create(Member member, ReservationOption option,
                                     LocalDate date, LocalTime startTime, LocalTime endTime) {
        validateTime(startTime, endTime);

        Reservation reservation = new Reservation();
        reservation.member = member;
        reservation.option = option;
        reservation.reservationDate = date;
        reservation.startTime = startTime;
        reservation.endTime = endTime;
        reservation.status = ReservationStatus.RESERVED;
        return reservation;
    }

    private static void validateTime(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }
    }

    public void cancel(UUID memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new CustomException(ErrorCode.NO_RESERVATION_PERMISSION);
        }
        if (this.status != ReservationStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ReservationStatus.CANCELED;
    }
}