package com.staysafe.console;

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

    public LoadAmenities(OverpassService overpassService) {
        this.overpassService = overpassService;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        List<OverpassPoiDTO> amenities = this.overpassService.getAmenitiesForArea("nightclub", "Hamburg");
        System.out.println("Loading Amenities");
        System.out.println("Found " + amenities.size() + " Amenities");
        System.out.println("first amenity is " + amenities.getFirst().toString());
        System.exit(0);
    }
}
