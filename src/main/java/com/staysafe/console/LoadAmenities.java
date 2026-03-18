package com.staysafe.console;

import com.staysafe.config.AppConfig;
import com.staysafe.dto.OverpassPoiDTO;
import com.staysafe.services.overpass.OverpassService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("import-amenities")
public class LoadAmenities implements ApplicationRunner {
    private final OverpassService overpassService;
    private final AppConfig appConfig;

    public LoadAmenities(
            OverpassService overpassService,
            AppConfig appConfig
    ) {
        this.overpassService = overpassService;
        this.appConfig = appConfig;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        List<OverpassPoiDTO> amenitiesToSave;
        List<String> areas = this.appConfig.getAreas();
        List<String> amenitiesToGet = this.appConfig.getAmenities();

        for (String area : areas) {
            List<OverpassPoiDTO> amenities = amenitiesToGet.stream().map(e -> {
                return this.overpassService.getAmenitiesForArea(e, area);
            }).toList();
        }
        for (OverpassPoiDTO amenity : amenities) {

        }
        System.exit(0);
    }
}
