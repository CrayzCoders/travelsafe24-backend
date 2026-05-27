package com.staysafe.application.controller.matchingscore;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single scoring criterion and its value for a district")
public class CriteriaDTO {

    @Schema(description = "Criterion name (e.g. kindergarten, bar, centrality)", example = "kindergarten")
    private final String name;

    @Schema(description = "Raw POI count for amenity criteria, or normalised centrality score (0–1) for the centrality criterion", example = "14.0")
    private final double value;

    public CriteriaDTO(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
