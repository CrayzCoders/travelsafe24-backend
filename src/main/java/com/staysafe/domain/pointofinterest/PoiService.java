package com.staysafe.domain.pointofinterest;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoiService {
    private final PoiTypeRepository poiTypeRepository;
    private final PointOfInterestRepository pointOfInterestRepository;

    public PoiService(
            PoiTypeRepository poiTypeRepository,
            PointOfInterestRepository pointOfInterestRepository
    ) {
        this.poiTypeRepository = poiTypeRepository;
        this.pointOfInterestRepository = pointOfInterestRepository;
    }

    public PoiType findOrCreateType(String typeName) {
        return poiTypeRepository.findByName(typeName)
            .orElseGet(
                () -> {
                    PoiType newType = new PoiType(typeName);
                    return poiTypeRepository.save(newType);
                }
            );
    }

    public Optional<PointOfInterest> findByOsmId(long osmId) {
        return pointOfInterestRepository.findByOsmId(osmId);
    }

    public void savePointOfInterest(PointOfInterest pointOfInterest) {
        pointOfInterestRepository.saveAndFlush(pointOfInterest);
    }

    public List<PointOfInterest> findInsidePolygon(@Param("polygon") Polygon polygon) {
        return pointOfInterestRepository.findInsidePolygon(polygon);
    }

    public List<PointOfInterest> findPois(String typeName, String districtName) {
        boolean hasType = typeName != null && !typeName.isBlank();
        boolean hasDistrict = districtName != null && !districtName.isBlank();

        if (hasType && hasDistrict) {
            return pointOfInterestRepository.findByTypeNameAndDistrictName(typeName, districtName);
        }
        if (hasType) return pointOfInterestRepository.findByTypeName(typeName);
        if (hasDistrict) return pointOfInterestRepository.findByDistrictName(districtName);
        return pointOfInterestRepository.findAll();
    }

    public List<PoiType> getAllPoiTypes() {
        return poiTypeRepository.findAll();
    }
}
