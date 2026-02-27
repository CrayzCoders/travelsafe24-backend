package com.staysafe.services.overpass;

import com.staysafe.config.AppConfig;
import com.staysafe.dto.OverpassPoiDTO;
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

    public OverpassService(
            AppConfig appConfig,
            AreaService areaService,
            AmenityService amenityService
    ) {
        this.areaService = areaService;
        this.amenityService = amenityService;
        this.client = WebClient.create(appConfig.getOverpassUrl());
    }

    public List<OverpassPoiDTO> getAmenitiesForArea(String amenity, String area) {
        if (!this.areaService.isValidArea(area)) {
            return List.of();
        }
        if (!this.amenityService.isValidAmenity(amenity)) {
            return List.of();
        }
        String query = "[out:json][timeout:750];area[name=\"" + area + "\"][boundary=\"administrative\"]->.searchArea;(node[\"amenity\"=\"" + amenity + "\"](area.searchArea);way[\"amenity\"=\"" + amenity + "\"](area.searchArea);relation[\"amenity\"=\"" + amenity + "\"](area.searchArea););out center;";

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

    private List<OverpassPoiDTO> getMappedResponse(String response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OverpassResponseDTO responseDto = mapper.readValue(response, OverpassResponseDTO.class);

            return responseDto.getElements().stream()
                    .map(e -> {
                        long id = e.getId();
                        double lat = (e.getLat() != null) ? e.getLat() : e.getCenter().getLat();
                        double lon = (e.getLon() != null) ? e.getLon() : e.getCenter().getLon();
                        String type = e.getType();
                        String name = (e.getTags() != null) ? e.getTags().getName() : null;

                        return new OverpassPoiDTO(id, type, lon, lat, name);
                    }).toList();
        } catch (Exception e) {
            return List.of();
        }
    }
}
