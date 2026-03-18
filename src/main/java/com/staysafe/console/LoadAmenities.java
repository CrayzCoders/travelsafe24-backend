package com.staysafe.console;

import com.staysafe.config.AppConfig;
import com.staysafe.database.entities.District;
import com.staysafe.database.entities.PoiType;
import com.staysafe.database.entities.PointOfInterest;
import com.staysafe.dto.OverpassPoiDTO;
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
    public void run(@NonNull ApplicationArguments args) {
        List<OverpassPoiDTO> amenitiesToSave = new ArrayList<>();
        List<String> areas = this.appConfig.getAreas();
        List<String> amenitiesToGet = this.appConfig.getAmenities();

        for (String area : areas) {
            for (String amenity : amenitiesToGet) {
                amenitiesToSave.addAll(this.overpassService.getAmenitiesForArea(amenity, area));
            }
        }

        for (OverpassPoiDTO amenity : amenitiesToSave) {
            if (this.poiService.findByOsmId(amenity.id()).isEmpty()) {
                this.createPointOfInterest(amenity);
            }
        }

        SpringApplication.exit(context);
    }

    private void createPointOfInterest(OverpassPoiDTO amenity) {
        Point location = GeometryUtils.createPoint(amenity.lat(), amenity.lon());
        Optional<District> district = this.districtService.findByPoint(location);
        if (district.isPresent()) {
            PoiType type = this.poiService.findOrCreateType(amenity.name());
            PointOfInterest pointOfInterest = new PointOfInterest(
                amenity.id(),
                amenity.name(),
                location,
                district.get(),
                type
            );

            this.poiService.savePointOfInterest(pointOfInterest);
        }
    }
}
