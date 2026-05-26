package com.staysafe.domain.district;

import com.staysafe.domain.city.City;

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

    Optional<District> findDistrictByName(String districtName);

    @Query(value = """
    SELECT ST_IsValid(ST_Multi(ST_UnaryUnion(ST_Collect(d.polygon))))
    FROM districts d
    WHERE d.city_id = :cityId
    """, nativeQuery = true)
    boolean isValidUnionByCity(Long cityId);

    @Query(value = """
    SELECT ST_Multi(ST_UnaryUnion(ST_Collect(d.polygon)))
    FROM districts d
    WHERE d.city_id = :cityId
    """, nativeQuery = true)
    Object unionByCity(Long cityId);
}
