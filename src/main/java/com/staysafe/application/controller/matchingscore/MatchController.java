package com.staysafe.application.controller.matchingscore;

import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.city.CityService;
import com.staysafe.domain.matchingscore.MatchingScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Matching Scores", description = "Compute weighted district matching scores based on user lifestyle preferences")
@RestController
public class MatchController {
    private final CityService cityService;
    private final MatchingScoreService matchingScoreService;

    public  MatchController(
        CityService cityService,
        MatchingScoreService matchingScoreService
    ) {
        this.cityService = cityService;
        this.matchingScoreService = matchingScoreService;
    }

    @Operation(
            summary = "Calculate district matching scores",
            description = "Calculates a matching score (0–100) for every Hamburg district based on the user's " +
                    "preferences for family institutions, nightlife, and centrality. " +
                    "Scores are normalized across all districts and returned together with per-criteria detail."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Matching scores successfully calculated for all districts",
            content = @Content(schema = @Schema(implementation = MatchResponseDTO.class))
    )
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/get-matching-scores")
    public MatchResponseDTO getMatchingScores(@RequestBody MatchFormRequestDTO requestData) {
        Map<String, Double> normalizedPreferences = matchingScoreService.normalizeUserPreferences(requestData);
        Map<String, Double> weights = matchingScoreService.normalizeUserWeights(requestData);
        List<District> districts = this.cityService.getAllDistricts();

        Map<String, Double> medianDensities = matchingScoreService.computeMedianDensities(districts);
        Map<String, Double> centralities = matchingScoreService.computeNormalizedCentralities(districts);
        Map<String, DistrictDTO> districtResults = new HashMap<>();
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;

        for (District district: districts) {
            Map<String, Double> normDistrictData = matchingScoreService.getNormalizedDistrictData(district, medianDensities);
            normDistrictData.put("centrality", centralities.get(district.getName()));

            double matchingScore = matchingScoreService.computeMatchingScore(normalizedPreferences, weights, normDistrictData);

            minScore = Math.min(minScore, matchingScore);
            maxScore = Math.max(maxScore, matchingScore);

            Map<String, List<PointOfInterest>> sortedPoi = matchingScoreService.sortPointsOfInterest(district.getPoi());

            List<CriteriaDTO> criteriaList = new ArrayList<>();

            for (String key: normalizedPreferences.keySet()) {
                if (key.equals("centrality")) {
                    criteriaList.add(new CriteriaDTO(key, normDistrictData.get("centrality")));
                } else {
                    double amount = sortedPoi.getOrDefault(key, List.of()).size();
                    criteriaList.add(new CriteriaDTO(key, amount));
                }
            }

            districtResults.put(
                    district.getName(),
                    new DistrictDTO(matchingScore, criteriaList)
            );
        }

        MatchResponseInfo info = new MatchResponseInfo("Hamburg", minScore, maxScore);

        return new MatchResponseDTO(info, districtResults);
    }
}