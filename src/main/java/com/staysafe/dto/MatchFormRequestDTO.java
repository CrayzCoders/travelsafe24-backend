package com.staysafe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchFormRequestDTO {
    @JsonProperty("needsKitasSchoolsUnis")
    private Boolean needsFamilyInstitutes;
    @JsonProperty("importanceKitas")
    private Integer kindergartenRating;
    @JsonProperty("importanceSchulen")
    private Integer schoolRating;
    @JsonProperty("importanceUnis")
    private Integer universityRating;
    private Integer age;
    private String profession;
    @JsonProperty("goesToBarsClubs")
    private Boolean needsNightlife;
    @JsonProperty("importanceClubs")
    private Integer clubRating;
    @JsonProperty("importanceBars")
    private Integer barRating;
    @JsonProperty("wantsCentralLiving")
    private Boolean needsCentrality;
    @JsonProperty("importanceCityCenter")
    private Integer centralityRating;
    @JsonProperty("importancePublicTransport")
    private Integer pubTransRating;

    public MatchFormRequestDTO() {}

    public Boolean isNeedsFamilyInstitutes() {
        return needsFamilyInstitutes;
    }

    public Integer getKindergartenRating() {
        return kindergartenRating;
    }

    public Integer getSchoolRating() {
        return schoolRating;
    }

    public Integer getUniversityRating() {
        return universityRating;
    }

    public Integer getAge() {
        return age;
    }

    public String getProfession() {
        return profession;
    }

    public Boolean isNeedsNightlife() {
        return needsNightlife;
    }

    public Integer getClubRating() {
        return clubRating;
    }

    public Integer getBarRating() {
        return barRating;
    }

    public Boolean isNeedsCentrality() {
        return needsCentrality;
    }

    public Integer getCentralityRating() {
        return centralityRating;
    }

    public Integer getPubTransRating() {
        return pubTransRating;
    }
}
