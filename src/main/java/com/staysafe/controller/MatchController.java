package com.staysafe.controller;

import com.staysafe.database.entities.District;
import com.staysafe.database.entities.PointOfInterest;
import com.staysafe.dto.*;
import com.staysafe.services.city.CityService;
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
    private static final List<String> ALL_KEYS = List.of(
            "kindergarten",
            "school",
            "university",
            "bar",
            "nightclub"
    );

    public  MatchController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping("/get-matching-scores")
    public MatchResponseDTO getMatchingScores(@RequestBody MatchFormRequestDTO requestData) {
        Map<String, Double> normalizedPreferences = this.normalizeUserPreferences(requestData);
        List<District> districts = this.cityService.getAllDistricts();

        Map<String, Double> min = this.getMinValues(districts);
        Map<String, Double> max = this.getMaxValues(districts);
        Map<String, DistrictDTO> districtResults = new HashMap<>();
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;

        for (District district : districts) {
            Map<String, Double> normDistrictData = this.getNormalizedDistrictData(district, min, max);
            double matchingScore = this.computeMatchingScore(normalizedPreferences, normDistrictData);

            minScore = Math.min(minScore, matchingScore);
            maxScore = Math.max(maxScore, matchingScore);

            Map<String, List<PointOfInterest>> sortedPoi = this.sortPointsOfInterest(district.getPoi());

            List<CriteriaDTO> criteriaList = new ArrayList<>();

            for (String key : normalizedPreferences.keySet()) {
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

    private Map<String, Double> normalizeUserPreferences(MatchFormRequestDTO requestData) {
        Map<String, Integer> preferences = new HashMap<>();
        preferences.put("kindergarten", requestData.getKindergartenRating() != null ? requestData.getKindergartenRating() : 1);
        preferences.put("school", requestData.getSchoolRating() != null ? requestData.getSchoolRating() : 1);
        preferences.put("university", requestData.getUniversityRating() != null ? requestData.getUniversityRating() : 1);
        preferences.put("nightclub", requestData.getClubRating() != null ? requestData.getClubRating() : 1);
        preferences.put("bar", requestData.getBarRating() != null ? requestData.getBarRating() : 1);

        Map<String, Double> weights = new HashMap<>();
        for (Map.Entry<String, Integer> entry : preferences.entrySet()) {
            double weight = (entry.getValue() - 1) / 4.0;
            weights.put(entry.getKey(), weight);
        }

        return weights;
    }

    private Map<String, List<PointOfInterest>> sortPointsOfInterest(List<PointOfInterest> pointsOfInterest) {
        Map<String, List<PointOfInterest>> sortedPoi = new HashMap<>();
        for (PointOfInterest poi : pointsOfInterest) {
            String typeName = poi.getType().getName();
            if (typeName.equals("kindergarten") ||  typeName.equals("childcare")) {
                sortedPoi.computeIfAbsent("kindergarten", k -> new ArrayList<>()).add(poi);
            } else if (typeName.equals("university") || typeName.equals("college")) {
                sortedPoi.computeIfAbsent("university", k -> new ArrayList<>()).add(poi);
            } else {
                sortedPoi.computeIfAbsent(typeName, k -> new ArrayList<>()).add(poi);
            }
        }

        return sortedPoi;
    }

    private Map<String, Double> getMinValues(List<District> districts) {
        Map<String, Double> min = new HashMap<>();
        for (District district : districts) {
            Map<String, List<PointOfInterest>> sortedPoi = this.sortPointsOfInterest(district.getPoi());
            double area = district.getPolygon().getArea();
            for (String key : ALL_KEYS) {
                double density = sortedPoi.getOrDefault(key, List.of()).size() / area;
                min.merge(key, density, Math::min);
            }
        }

        return min;
    }

    private Map<String, Double> getMaxValues(List<District> districts) {
        Map<String, Double> max = new HashMap<>();
        for (District district : districts) {
            Map<String, List<PointOfInterest>> sortedPoi = this.sortPointsOfInterest(district.getPoi());
            double area = district.getPolygon().getArea();
            for (String key : ALL_KEYS) {
                double density = sortedPoi.getOrDefault(key, List.of()).size() / area;
                max.merge(key, density, Math::max);
            }
        }

        return max;
    }

    private Map<String, Double> getNormalizedDistrictData(
            District district,
            Map<String, Double> min,
            Map<String, Double> max
    ) {
        Map<String, List<PointOfInterest>> sortedPoi = this.sortPointsOfInterest(district.getPoi());
        Map<String, Double> normData = new HashMap<>();
        for (String key : ALL_KEYS) {
            double minimum = min.get(key);
            double maximum = max.get(key);
            double density = sortedPoi.getOrDefault(key, List.of()).size() / district.getPolygon().getArea();
            double normalized;

            if (maximum - minimum == 0) {
                normalized = 1.0;
            } else {
                normalized = (density - minimum) / (maximum - minimum);
            }
            normData.put(key, normalized);
        }

        return normData;
    }

    private double computeMatchingScore(Map<String, Double> weights, Map<String, Double> normData) {
        double scoreSum = 0;
        double weightSum = 0;

        for (String key : weights.keySet()) {
            double value = normData.getOrDefault(key, 0.0);
            scoreSum += value * weights.get(key);
            weightSum += weights.get(key);
        }

        if (weightSum == 0) return 0;
        return (scoreSum / weightSum) * 100;
    }
}
