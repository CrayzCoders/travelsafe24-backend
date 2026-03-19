package com.staysafe.dto;

import java.util.List;

public class DistrictDTO {
    private final double matchingScore;
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
