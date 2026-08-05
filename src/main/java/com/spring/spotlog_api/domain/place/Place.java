package com.spring.spotlog_api.domain.place;

import com.spring.spotlog_api.domain.member.Member;
import com.spring.spotlog_api.domain.place.dto.PlaceUpdateRequest;
import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "place_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceCategory category;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "jibun_address", nullable = false)
    private String jibunAddress;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PlaceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;

    private Place(Member owner, PlaceCategory category, String name,
                  String description, String roadAddress, String jibunAddress,
                  String postalCode, Double latitude, Double longitude,
                  LocalTime openTime, LocalTime closeTime) {
        validateTime(openTime, closeTime);
        validateLocation(latitude, longitude);
        this.owner = owner;
        this.category = category;
        this.name = name.trim();
        this.description = description;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.status = PlaceStatus.ACTIVE;
    }

    public static Place create(Member owner, PlaceCategory category, String name,
                               String description, String roadAddress, String jibunAddress,
                               String postalCode, Double latitude, Double longitude,
                               LocalTime openTime, LocalTime closeTime) {
        return new Place(owner, category, name, description, roadAddress,
                jibunAddress, postalCode, latitude, longitude, openTime, closeTime);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 장소 비활성화
    public void deactivate() {
        if (this.status == PlaceStatus.INACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_INACTIVE_PLACE);
        }
        this.status = PlaceStatus.INACTIVE;
    }

    // 장소 활성화
    public void activate() {
        if (this.status == PlaceStatus.ACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_ACTIVE_PLACE);
        }
        this.status = PlaceStatus.ACTIVE;
    }

    // 영업 중인지 확인
    public boolean isOpenNow() {
        LocalTime now = LocalTime.now();
        if (openTime.isBefore(closeTime)) {
            return !now.isBefore(openTime) && now.isBefore(closeTime);
        }
        return !now.isBefore(openTime) || now.isBefore(closeTime);
    }

    // 본인 장소인지 확인
    public boolean isOwnedBy(UUID memberId) {
        return this.owner.getId().equals(memberId);
    }

    private static void validateTime(LocalTime openTime, LocalTime closeTime) {
        if (openTime != null && closeTime != null) {
            if (!openTime.isBefore(closeTime)) {
                throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
            }
        }
    }

    private static void validateLocation(Double lat, Double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new CustomException(ErrorCode.INVALID_LOCATION);
        }
    }

    // 장소 수정
    public void update(PlaceUpdateRequest request) {
        if (request.name() != null) this.name = request.name().trim();
        if (request.description() != null) this.description = request.description();
        if (request.roadAddress() != null) this.roadAddress = request.roadAddress();
        if (request.jibunAddress() != null) this.jibunAddress = request.jibunAddress();
        if (request.postalCode() != null) this.postalCode = request.postalCode();
        if (request.latitude() != null && request.longitude() != null) {
            validateLocation(request.latitude(), request.longitude());
            this.latitude = request.latitude();
            this.longitude = request.longitude();
        }
        if (request.openTime() != null || request.closeTime() != null) {
            LocalTime newOpenTime = request.openTime() != null ? request.openTime() : this.openTime;
            LocalTime newCloseTime = request.closeTime() != null ? request.closeTime() : this.closeTime;
            validateTime(newOpenTime, newCloseTime);
            this.openTime = newOpenTime;
            this.closeTime = newCloseTime;
        }
    }

    // 장소 삭제 (소프트 삭제)
    public void delete() {
        if (this.deleted) {
            throw new CustomException(ErrorCode.ALREADY_DELETED_PLACE);
        }
        this.deleted = true;
    }
}