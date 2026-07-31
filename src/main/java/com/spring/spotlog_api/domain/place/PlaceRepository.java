package com.spring.spotlog_api.domain.place;

import com.spring.spotlog_api.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID> {
    // OWNER의 장소 목록 조회
    Page<Place> findByOwner(Member owner, Pageable pageable);

    // 전체 장소 목록 조회 (카테고리 필터)
    Page<Place> findByStatus(PlaceStatus status, Pageable pageable);

    Page<Place> findByCategoryAndStatus(
            PlaceCategory category, PlaceStatus status, Pageable pageable);

    // 같은 OWNER + 장소명 + 도로명주소 중복 체크
    boolean existsByOwnerAndNameAndRoadAddress(
            Member owner, String name, String roadAddress);
}
