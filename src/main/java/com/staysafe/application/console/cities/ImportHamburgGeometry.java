package com.staysafe.application.console.cities;

import com.staysafe.domain.city.City;
import com.staysafe.domain.district.District;
import com.staysafe.domain.district.DistrictRepository;
import com.staysafe.domain.city.CityService;
import jakarta.transaction.Transactional;
import org.geolatte.geom.jts.JTS;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.Geometry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wololo.jts2geojson.GeoJSONReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Profile("import-geometry")
public class ImportHamburgGeometry implements ApplicationRunner {
    private final DistrictRepository districtRepository;
    private final CityService cityService;

    public ImportHamburgGeometry(
            DistrictRepository districtRepository,
            CityService cityService
    ) {
        this.districtRepository = districtRepository;
        this.cityService = cityService;
    }

    @Transactional
    @Override
    public void run(@NonNull ApplicationArguments args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        FeaturesDTO features = mapper.readValue(
            new File("src/main/resources/import_data/districts/hamburg.json"),FeaturesDTO.class
        );
        List<GeoJsonDistrictDTO> geoJsons = features.getFeatures();
        GeoJSONReader reader = new GeoJSONReader();

        City city = this.cityService.findOrCreate("Hamburg", "Germany");

        for (GeoJsonDistrictDTO geoJson : geoJsons) {

            String districtName = geoJson.getProperties().getDistrict();
            
            if(cityService.findDistrict(districtName, city).isEmpty()) {

                String geometryJson = mapper.writeValueAsString(geoJson.getGeometry());
                Geometry geometry = reader.read(geometryJson);
                geometry.setSRID(4326);

                District district = new District(districtName, city, geometry);

                districtRepository.save(district);
            }
        }

        boolean validGeometry = districtRepository.isValidUnionByCity(city.getId());
        if (validGeometry) {
            org.geolatte.geom.Geometry<?> geom =
                    (org.geolatte.geom.Geometry<?>) districtRepository.unionByCity(city.getId());

            city.setGeometry(JTS.to(geom));
            cityService.saveCity(city);
        } else {
            throw new RuntimeException("invalid geometry while fetching boundary box of the city");
        }
    }
}
