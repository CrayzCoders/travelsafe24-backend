package com.staysafe.database.repositories;

import com.staysafe.database.entities.City;
import com.staysafe.database.entities.District;

import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByNameAndCity(String name, City city);

    @Query("SELECT d FROM District d WHERE within(:point, d.polygon) = true")
    Optional<District> findByPoint(@Param("point") Point point);

    List<District> findDistrictsByCity(City city);
}
