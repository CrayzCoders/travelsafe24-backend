package com.staysafe.application.controller.pointofinterest;

import com.staysafe.domain.pointofinterest.PoiType;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class PoiController {
    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @GetMapping("/pois")
    public List<PoiResponseDTO> getPois(
            @RequestParam(name = "type", required = false) String typeName,
            @RequestParam(name = "district", required = false) String districtName
    ) {
        List<PointOfInterest> pois = poiService.findPois(typeName, districtName);

        return pois.stream()
                .map(PoiResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/poi-types")
    public List<PoiType> getAllPoiTypes() {
        return  poiService.getAllPoiTypes();
    }
}
