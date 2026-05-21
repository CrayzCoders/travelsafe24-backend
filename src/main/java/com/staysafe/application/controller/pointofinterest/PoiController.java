package com.staysafe.application.controller.pointofinterest;

import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PoiController {
    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/pois")
    public List<PoiResponseDTO> getPoisByType(@RequestParam(name = "type") String typeName) {
        if (typeName == null || typeName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameter 'type' is required");
        }

        List<PointOfInterest> pois = poiService.findByPoiTypeName(typeName);
        return pois.stream()
            .map(PoiResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
