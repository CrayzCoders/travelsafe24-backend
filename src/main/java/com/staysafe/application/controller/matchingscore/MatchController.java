package com.staysafe.application.controller.matchingscore;

import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.city.CityService;
import com.staysafe.domain.matchingscore.MatchingScoreService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/get-matching-scores")
    public MatchResponseDTO getMatchingScores(@RequestBody MatchFormRequestDTO requestData) {
        Map<String, Double> normalizedPreferences = matchingScoreService.normalizeUserPreferences(requestData);
        List<District> districts = this.cityService.getAllDistricts();

        Map<String, Double> min = matchingScoreService.getMinValues(districts);
        Map<String, Double> max = matchingScoreService.getMaxValues(districts);
        Map<String, DistrictDTO> districtResults = new HashMap<>();
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;

        for (District district: districts) {
            Map<String, Double> normDistrictData = matchingScoreService.getNormalizedDistrictData(district, min, max);
            double matchingScore = matchingScoreService.computeMatchingScore(normalizedPreferences, normDistrictData);

            minScore = Math.min(minScore, matchingScore);
            maxScore = Math.max(maxScore, matchingScore);

            Map<String, List<PointOfInterest>> sortedPoi = matchingScoreService.sortPointsOfInterest(district.getPoi());

            List<CriteriaDTO> criteriaList = new ArrayList<>();

            for (String key: normalizedPreferences.keySet()) {
                double amount = sortedPoi.getOrDefault(key, List.of()).size();

                criteriaList.add(new CriteriaDTO(key, amount));
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
