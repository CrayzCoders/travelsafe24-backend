package com.staysafe.controller;

import com.staysafe.database.entities.District;
import com.staysafe.database.entities.PointOfInterest;
import com.staysafe.dto.*;
import com.staysafe.services.city.CityService;
import com.staysafe.services.match.MatchService;
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
    private final MatchService matchService;

    public  MatchController(
        CityService cityService,
        MatchService matchService
    ) {
        this.cityService = cityService;
        this.matchService = matchService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/get-matching-scores")
    public MatchResponseDTO getMatchingScores(@RequestBody MatchFormRequestDTO requestData) {
        Map<String, Double> normalizedPreferences = matchService.normalizeUserPreferences(requestData);
        List<District> districts = this.cityService.getAllDistricts();

        Map<String, Double> min = matchService.getMinValues(districts);
        Map<String, Double> max = matchService.getMaxValues(districts);
        Map<String, DistrictDTO> districtResults = new HashMap<>();
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;

        for (District district: districts) {
            Map<String, Double> normDistrictData = matchService.getNormalizedDistrictData(district, min, max);
            double matchingScore = matchService.computeMatchingScore(normalizedPreferences, normDistrictData);

            minScore = Math.min(minScore, matchingScore);
            maxScore = Math.max(maxScore, matchingScore);

            Map<String, List<PointOfInterest>> sortedPoi = matchService.sortPointsOfInterest(district.getPoi());

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
