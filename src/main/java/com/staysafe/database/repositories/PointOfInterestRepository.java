package com.staysafe.database.repositories;

import com.staysafe.database.entities.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {
    Optional<PointOfInterest> findByOsmId(long osmId);
}
