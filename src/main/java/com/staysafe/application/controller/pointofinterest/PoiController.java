package com.staysafe.application.controller.pointofinterest;

import com.staysafe.domain.pointofinterest.PoiType;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Points of Interest", description = "Query POIs and POI types stored in the database")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class PoiController {
    private final PoiService poiService;

    public PoiController(PoiService poiService) {
        this.poiService = poiService;
    }

    @Operation(
            summary = "Get points of interest",
            description = "Returns all POIs. Use the optional query parameters to filter by type and/or district. " +
                    "If neither parameter is provided all POIs are returned."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of matching POIs",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PoiResponseDTO.class)))
    )
    @GetMapping("/pois")
    public List<PoiResponseDTO> getPois(
            @Parameter(description = "Filter by POI type name (e.g. bar, kindergarten, school, nightclub)")
            @RequestParam(name = "type", required = false) String typeName,
            @Parameter(description = "Filter by district name (e.g. Altona, Eimsbüttel)")
            @RequestParam(name = "district", required = false) String districtName
    ) {
        List<PointOfInterest> pois = poiService.findPois(typeName, districtName);

        return pois.stream()
                .map(PoiResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Get all POI types",
            description = "Returns the list of all known POI type names (e.g. bar, kindergarten, school, nightclub, university)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all POI types",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PoiType.class)))
    )
    @GetMapping("/poi-types")
    public List<PoiType> getAllPoiTypes() {
        return  poiService.getAllPoiTypes();
    }
}
