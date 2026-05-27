package com.staysafe.domain.matchingscore;

import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.application.controller.matchingscore.MatchFormRequestDTO;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchingScoreService {
    private static final List<String> ALL_KEYS = List.of(
            "kindergarten",
            "school",
            "university",
            "bar",
            "nightclub"
    );

    private static final double DEFAULT_EXCLUSION_WEIGHT = 0.6;
    private static final double SATURATION_FACTOR = 4.0;

    // boolean=true  → target 1.0 (want more of this)
    // boolean=false → target 0.0 (penalise districts that have lots of this)
    // boolean=null  → key absent (exclude from score)
    public Map<String, Double> normalizeUserPreferences(MatchFormRequestDTO requestData) {
        Map<String, Double> targets = new HashMap<>();
        addTargets(targets, requestData.isNeedsFamilyInstitutes(), "kindergarten", "school", "university");
        addTargets(targets, requestData.isNeedsNightlife(), "bar", "nightclub");
        addTargets(targets, requestData.isNeedsCentrality(), "centrality");
        return targets;
    }

    // boolean=true  → weight from the user's importance rating
    // boolean=false → fixed moderate weight (user gave no rating, but penalty still applies)
    // boolean=null  → key absent
    public Map<String, Double> normalizeUserWeights(MatchFormRequestDTO requestData) {
        Map<String, Double> weights = new HashMap<>();

        if (requestData.isNeedsFamilyInstitutes() != null) {
            if (Boolean.TRUE.equals(requestData.isNeedsFamilyInstitutes())) {
                weights.put("kindergarten", toWeight(requestData.getKindergartenRating()));
                weights.put("school", toWeight(requestData.getSchoolRating()));
                weights.put("university", toWeight(requestData.getUniversityRating()));
            } else {
                weights.put("kindergarten", DEFAULT_EXCLUSION_WEIGHT);
                weights.put("school", DEFAULT_EXCLUSION_WEIGHT);
                weights.put("university", DEFAULT_EXCLUSION_WEIGHT);
            }
        }

        if (requestData.isNeedsNightlife() != null) {
            if (Boolean.TRUE.equals(requestData.isNeedsNightlife())) {
                weights.put("bar", toWeight(requestData.getBarRating()));
                weights.put("nightclub", toWeight(requestData.getClubRating()));
            } else {
                weights.put("bar", DEFAULT_EXCLUSION_WEIGHT);
                weights.put("nightclub", DEFAULT_EXCLUSION_WEIGHT);
            }
        }

        if (requestData.isNeedsCentrality() != null) {
            if (Boolean.TRUE.equals(requestData.isNeedsCentrality())) {
                weights.put("centrality", toWeight(requestData.getCentralityRating()));
            } else {
                weights.put("centrality", DEFAULT_EXCLUSION_WEIGHT);
            }
        }

        return weights;
    }

    private void addTargets(Map<String, Double> targets, Boolean flag, String... keys) {
        if (flag == null) return;
        double target = Boolean.TRUE.equals(flag) ? 1.0 : 0.0;
        for (String key : keys) targets.put(key, target);
    }

    private double toWeight(Integer rating) {
        return (rating != null ? rating : 3) / 5.0;
    }

    public Map<String, Double> computeMedianDensities(List<District> districts) {
        Map<String, List<Double>> densitiesByKey = new HashMap<>();
        for (String key : ALL_KEYS) densitiesByKey.put(key, new ArrayList<>());

        for (District district : districts) {
            Map<String, List<PointOfInterest>> sortedPoi = sortPointsOfInterest(district.getPoi());
            double area = district.getPolygon().getArea();
            for (String key : ALL_KEYS) {
                double density = sortedPoi.getOrDefault(key, List.of()).size() / area;
                densitiesByKey.get(key).add(density);
            }
        }

        Map<String, Double> medians = new HashMap<>();
        for (String key : ALL_KEYS) {
            List<Double> sorted = densitiesByKey.get(key);
            Collections.sort(sorted);
            int n = sorted.size();
            double median = n % 2 == 0
                    ? (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0
                    : sorted.get(n / 2);
            medians.put(key, median);
        }
        return medians;
    }

    public Map<String, Double> getNormalizedDistrictData(
            District district,
            Map<String, Double> medianDensities
    ) {
        Map<String, List<PointOfInterest>> sortedPoi = sortPointsOfInterest(district.getPoi());
        Map<String, Double> normData = new HashMap<>();
        for (String key : ALL_KEYS) {
            double threshold = medianDensities.get(key) * SATURATION_FACTOR;
            double density = sortedPoi.getOrDefault(key, List.of()).size() / district.getPolygon().getArea();
            double normalized = threshold == 0 ? 1.0 : Math.min(density / threshold, 1.0);
            normData.put(key, normalized);
        }
        return normData;
    }

    public double computeMatchingScore(
            Map<String, Double> targets,
            Map<String, Double> weights,
            Map<String, Double> normData
    ) {
        double scoreSum = 0;
        double weightSum = 0;

        for (String key : targets.keySet()) {
            double target = targets.get(key);
            double weight = weights.getOrDefault(key, 1.0);
            double actual = normData.getOrDefault(key, 0.0);
            scoreSum += weight * (1.0 - Math.abs(target - actual));
            weightSum += weight;
        }

        if (weightSum == 0) return 0;
        return (scoreSum / weightSum) * 100;
    }

    public Map<String, Double> computeNormalizedCentralities(List<District> districts) {
        Geometry cityUnion = districts.stream()
                .map(District::getPolygon)
                .reduce(Geometry::union)
                .orElseThrow();
        double centerX = cityUnion.getCentroid().getX();
        double centerY = cityUnion.getCentroid().getY();

        Map<String, Double> distances = new HashMap<>();
        for (District d : districts) {
            double dx = d.getPolygon().getCentroid().getX() - centerX;
            double dy = d.getPolygon().getCentroid().getY() - centerY;
            distances.put(d.getName(), Math.sqrt(dx * dx + dy * dy));
        }

        List<Double> sorted = new ArrayList<>(distances.values());
        Collections.sort(sorted);
        double minDist = sorted.getFirst();
        double avgGap = (sorted.getLast() - minDist) / (sorted.size() - 1);

        // Cap at the first gap that is more than twice the average gap — exclaves cause a sudden jump
        double effectiveMax = sorted.getLast();
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i + 1) - sorted.get(i) > avgGap * 2) {
                effectiveMax = sorted.get(i);
                break;
            }
        }
        double range = effectiveMax - minDist;

        Map<String, Double> centralities = new HashMap<>();
        for (Map.Entry<String, Double> e : distances.entrySet()) {
            double clampedDist = Math.min(e.getValue(), effectiveMax);
            double normalized = range == 0 ? 1.0 : 1.0 - (clampedDist - minDist) / range;
            centralities.put(e.getKey(), normalized);
        }
        return centralities;
    }

    public Map<String, List<PointOfInterest>> sortPointsOfInterest(List<PointOfInterest> pointsOfInterest) {
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
}
