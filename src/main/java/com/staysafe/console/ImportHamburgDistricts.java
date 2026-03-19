package com.staysafe.console;

import com.staysafe.database.entities.City;
import com.staysafe.database.entities.District;
import com.staysafe.database.repositories.DistrictRepository;
import com.staysafe.dto.GeoJsonDistrictDTO;
import com.staysafe.services.city.CityService;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Geometry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wololo.jts2geojson.GeoJSONReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Profile("import-districts")
public class ImportHamburgDistricts implements ApplicationRunner {
    private final DistrictRepository districtRepository;
    private final CityService cityService;
    private final ApplicationContext context;

    public ImportHamburgDistricts(
            DistrictRepository districtRepository,
            CityService cityService,
            ApplicationContext context
    ) {
        this.districtRepository = districtRepository;
        this.cityService = cityService;
        this.context = context;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<GeoJsonDistrictDTO> geoJsons = mapper.readValue(
            new File("src/main/resources/districts/hamburg.json"),
            mapper.getTypeFactory().constructCollectionType(List.class, GeoJsonDistrictDTO.class)
        );
        GeoJSONReader reader = new GeoJSONReader();

        City city = this.cityService.findOrCreate("Hamburg", "Germany");

        for (GeoJsonDistrictDTO geoJson : geoJsons) {
            String districtName = geoJson.getProperties().getDistrict();
            String geometryJson = mapper.writeValueAsString(geoJson.getGeometry());

            Geometry geometry = reader.read(geometryJson);
            geometry.setSRID(4326);

            District district = new District(districtName, city, geometry);

            districtRepository.save(district);
        }

        SpringApplication.exit(context);
    }
}
