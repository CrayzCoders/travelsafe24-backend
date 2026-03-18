package com.staysafe.services.city;

import com.staysafe.database.entities.District;
import com.staysafe.database.repositories.DistrictRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DistrictService {
    private final DistrictRepository districtRepository;

    public DistrictService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public Optional<District> findByPoint(Point point) {
        return this.districtRepository.findByPoint(point);
    }
}
