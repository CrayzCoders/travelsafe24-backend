package com.staysafe.domain.pointofinterest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {
    Optional<PointOfInterest> findByOsmId(long osmId);
    List<PointOfInterest> findByType(PoiType poiType);
}
