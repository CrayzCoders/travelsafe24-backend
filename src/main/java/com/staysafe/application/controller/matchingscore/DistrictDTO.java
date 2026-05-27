package com.staysafe.application.controller.matchingscore;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Matching result for a single district")
public class DistrictDTO {

    @Schema(description = "Weighted matching score for this district (0–100)", example = "73.5")
    private final double matchingScore;

    @Schema(description = "Per-criteria breakdown contributing to the matching score")
    private final List<CriteriaDTO> criteria;

    public DistrictDTO(double matchingScore, List<CriteriaDTO> criteria) {
        this.matchingScore = matchingScore;
        this.criteria = criteria;
    }

    public double getMatchingScore() {
        return matchingScore;
    }

    public List<CriteriaDTO> getCriteria() {
        return criteria;
    }
}
