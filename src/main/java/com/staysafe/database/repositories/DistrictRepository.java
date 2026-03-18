package com.staysafe.database.repositories;

import com.staysafe.database.entities.District;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {
    @Query("SELECT d FROM District d WHERE within(:point, d.polygon) = true")
    Optional<District> findByPoint(@Param("point") Point point);
}
