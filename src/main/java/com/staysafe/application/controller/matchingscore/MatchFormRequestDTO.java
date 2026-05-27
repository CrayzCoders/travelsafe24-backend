package com.staysafe.application.controller.matchingscore;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User lifestyle preference form used to compute district matching scores")
public class MatchFormRequestDTO {

    @Schema(description = "Whether the user needs family institutions (kindergartens, schools, universities)", example = "true")
    @JsonProperty("needsKitasSchoolsUnis")
    private Boolean needsFamilyInstitutes;

    @Schema(description = "Importance of kindergartens/childcare on a scale of 1 (low) to 5 (high)", example = "3", minimum = "1", maximum = "5")
    @JsonProperty("importanceKitas")
    private Integer kindergartenRating;

    @Schema(description = "Importance of schools on a scale of 1 (low) to 5 (high)", example = "4", minimum = "1", maximum = "5")
    @JsonProperty("importanceSchulen")
    private Integer schoolRating;

    @Schema(description = "Importance of universities/colleges on a scale of 1 (low) to 5 (high)", example = "2", minimum = "1", maximum = "5")
    @JsonProperty("importanceUnis")
    private Integer universityRating;

    @Schema(description = "User's age", example = "30")
    private Integer age;

    @Schema(description = "User's profession", example = "Software Engineer")
    private String profession;

    @Schema(description = "Whether the user frequents bars and clubs", example = "false")
    @JsonProperty("goesToBarsClubs")
    private Boolean needsNightlife;

    @Schema(description = "Importance of clubs/nightclubs on a scale of 1 (low) to 5 (high)", example = "1", minimum = "1", maximum = "5")
    @JsonProperty("importanceClubs")
    private Integer clubRating;

    @Schema(description = "Importance of bars on a scale of 1 (low) to 5 (high)", example = "2", minimum = "1", maximum = "5")
    @JsonProperty("importanceBars")
    private Integer barRating;

    @Schema(description = "Whether the user wants to live close to the city centre", example = "true")
    @JsonProperty("wantsCentralLiving")
    private Boolean needsCentrality;

    @Schema(description = "Importance of proximity to the city centre on a scale of 1 (low) to 5 (high)", example = "5", minimum = "1", maximum = "5")
    @JsonProperty("importanceCityCenter")
    private Integer centralityRating;

    @Schema(description = "Importance of public transport on a scale of 1 (low) to 5 (high)", example = "4", minimum = "1", maximum = "5")
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
