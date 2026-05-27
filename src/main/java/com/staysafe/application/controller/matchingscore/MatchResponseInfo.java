package com.staysafe.application.controller.matchingscore;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary metadata for a matching score calculation run")
public class MatchResponseInfo {

    @Schema(description = "City for which scores were calculated", example = "Hamburg")
    private final String city;

    @Schema(description = "Lowest matching score across all districts", example = "12.4")
    private final double minScore;

    @Schema(description = "Highest matching score across all districts", example = "87.6")
    private final double maxScore;

    public MatchResponseInfo(
            String city,
            double minScore,
            double maxScore
    ) {
        this.city = city;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getCity() {
        return city;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }
}
