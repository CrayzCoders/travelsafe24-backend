package com.staysafe.services.overpass;

import com.staysafe.config.AppConfig;
import com.staysafe.dto.OverpassResponseDTO;
import com.staysafe.services.amenity.AmenityService;
import com.staysafe.services.area.AreaService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;
import java.util.List;

@Service
public class OverpassService {
    private final WebClient client;
    private final AreaService areaService;
    private final AmenityService amenityService;
    private final int amenityLimit;

    public OverpassService(
            AppConfig appConfig,
            AreaService areaService,
            AmenityService amenityService
    ) {
        this.areaService = areaService;
        this.amenityService = amenityService;
        this.client = WebClient.create(appConfig.getOverpassUrl());
        this.amenityLimit = appConfig.getAmenityLimit();
    }

    public List<OverpassResponseDTO.Element> getAmenitiesForArea(String amenity, String area) {
        if (!this.areaService.isValidArea(area)) {
            return List.of();
        }
        if (!this.amenityService.isValidAmenity(amenity)) {
            return List.of();
        }
        String query = "[out:json][timeout:750];area[name=\"" + area + "\"][boundary=\"administrative\"]->.searchArea;(node[\"amenity\"=\"" + amenity + "\"](area.searchArea);way[\"amenity\"=\"" + amenity + "\"](area.searchArea);relation[\"amenity\"=\"" + amenity + "\"](area.searchArea););out " + this.amenityLimit + ";";

        return this.getMappedResponse(this.getResponse(query));
    }

    private String getResponse(String query) {
        return this.client.post()
                .uri("/interpreter")
                .bodyValue(query)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private List<OverpassResponseDTO.Element> getMappedResponse(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OverpassResponseDTO responseDto = mapper.readValue(response, OverpassResponseDTO.class);

            return responseDto.getElements();
        } catch (Exception e) {
            return List.of();
        }
    }
}
