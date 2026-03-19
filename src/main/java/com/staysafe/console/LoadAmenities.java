package com.staysafe.console;

import com.staysafe.config.AppConfig;
import com.staysafe.database.entities.District;
import com.staysafe.database.entities.PoiType;
import com.staysafe.database.entities.PointOfInterest;
import com.staysafe.dto.OverpassResponseDTO;
import com.staysafe.services.city.DistrictService;
import com.staysafe.services.geometry.GeometryUtils;
import com.staysafe.services.overpass.OverpassService;
import com.staysafe.services.poi.PoiService;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Profile("import-amenities")
public class LoadAmenities implements ApplicationRunner {
    private final OverpassService overpassService;
    private final AppConfig appConfig;
    private final ApplicationContext context;
    private final PoiService poiService;
    private final DistrictService districtService;

    public LoadAmenities(
            OverpassService overpassService,
            AppConfig appConfig,
            ApplicationContext context,
            PoiService poiService,
            DistrictService districtService
    ) {
        this.overpassService = overpassService;
        this.appConfig = appConfig;
        this.context = context;
        this.poiService = poiService;
        this.districtService = districtService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws InterruptedException {
        List<OverpassResponseDTO.Element> amenitiesToSave = new ArrayList<>();
        List<String> areas = this.appConfig.getAreas();
        List<String> amenitiesToGet = this.appConfig.getAmenities();

        for (String area : areas) {
            for (String amenity : amenitiesToGet) {
                try {
                    amenitiesToSave.addAll(this.overpassService.getAmenitiesForArea(amenity, area));
                } catch (Exception e) {
                }
                TimeUnit.SECONDS.sleep(2);
            }
        }

        for (OverpassResponseDTO.Element amenity : amenitiesToSave) {
            if (this.poiService.findByOsmId(amenity.getId()).isEmpty()) {
                this.createPointOfInterest(amenity);
            }
        }

        SpringApplication.exit(context);
    }

    private void createPointOfInterest(OverpassResponseDTO.Element amenity) {
        if (
                (amenity.getLat() == null && amenity.getCenter().getLat() == null)
                || (amenity.getLon() == null && amenity.getCenter().getLon() == null)
        ) {
            return;
        }
        double amenityLon = amenity.getLon() != null ?  amenity.getLon() : amenity.getCenter().getLon();
        double amenityLat = amenity.getLat() != null ?  amenity.getLat() : amenity.getCenter().getLat();

        Point location = GeometryUtils.createPoint(amenityLat, amenityLon);
        Optional<District> district = this.districtService.findByPoint(location);
        if (district.isPresent()) {
            OverpassResponseDTO.Tags tags = amenity.getTags();
            PoiType type = this.poiService.findOrCreateType(tags != null ? tags.getAmenityType() : "unknown");
            PointOfInterest pointOfInterest = new PointOfInterest(
                amenity.getId(),
                tags != null ? (tags.getName() != null ? tags.getName() : null) : null,
                location,
                district.get(),
                type
            );

            if (tags != null && tags.getZip() != null) {
                pointOfInterest.setZipCode(tags.getZip());
            }

            if (tags != null && tags.getStreet() != null) {
                pointOfInterest.setStreet(tags.getStreet());
            }

            if (tags != null && tags.getHouseNumber() != null) {
                pointOfInterest.setHouseNumber(tags.getHouseNumber());
            }

            this.poiService.savePointOfInterest(pointOfInterest);
        }
    }
}
