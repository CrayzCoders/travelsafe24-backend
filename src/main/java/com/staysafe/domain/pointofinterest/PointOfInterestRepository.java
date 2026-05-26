package com.staysafe.domain.pointofinterest;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {
    Optional<PointOfInterest> findByOsmId(long osmId);

    List<PointOfInterest> findByTypeName(String typeName);
    List<PointOfInterest> findByDistrictName(String districtName);
    List<PointOfInterest> findByTypeNameAndDistrictName(String typeName, String districtName);
    List<PointOfInterest> findByType(PoiType poiType);

    @Query(value = """
    SELECT poi.*
    FROM point_of_interests poi
    WHERE ST_Contains(:polygon, poi.location)
    """, nativeQuery = true)
    List<PointOfInterest> findInsidePolygon(@Param("polygon") Polygon polygon);
}
