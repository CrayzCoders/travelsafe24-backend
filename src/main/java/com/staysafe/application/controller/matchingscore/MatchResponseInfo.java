package com.staysafe.application.controller.matchingscore;

public class MatchResponseInfo {
    private final String city;
    private final double minScore;
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
