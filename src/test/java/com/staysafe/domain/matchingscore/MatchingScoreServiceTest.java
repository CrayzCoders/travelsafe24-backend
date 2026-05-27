package com.staysafe.domain.matchingscore;

import com.staysafe.application.controller.matchingscore.MatchFormRequestDTO;
import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchingScoreServiceTest {

    private MatchingScoreService service;
    private final GeometryFactory gf = new GeometryFactory();

    @BeforeEach
    void setUp() {
        service = new MatchingScoreService();
    }

    // --- normalizeUserPreferences ---

    @Test
    void normalizeUserPreferences_allNull_returnsEmptyMap() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(null);
        when(dto.isNeedsNightlife()).thenReturn(null);
        when(dto.isNeedsCentrality()).thenReturn(null);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).isEmpty();
    }

    @Test
    void normalizeUserPreferences_familyTrue_setsTargetsToOne() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(true);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result)
                .containsEntry("kindergarten", 1.0)
                .containsEntry("school", 1.0)
                .containsEntry("university", 1.0);
    }

    @Test
    void normalizeUserPreferences_familyFalse_setsTargetsToZero() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(false);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result)
                .containsEntry("kindergarten", 0.0)
                .containsEntry("school", 0.0)
                .containsEntry("university", 0.0);
    }

    @Test
    void normalizeUserPreferences_nightlifeTrue_setsBarAndNightclubToOne() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsNightlife()).thenReturn(true);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).containsEntry("bar", 1.0).containsEntry("nightclub", 1.0);
    }

    @Test
    void normalizeUserPreferences_nightlifeFalse_setsBarAndNightclubToZero() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsNightlife()).thenReturn(false);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).containsEntry("bar", 0.0).containsEntry("nightclub", 0.0);
    }

    @Test
    void normalizeUserPreferences_centralityTrue_setsCentralityToOne() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsCentrality()).thenReturn(true);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).containsEntry("centrality", 1.0);
    }

    @Test
    void normalizeUserPreferences_centralityFalse_setsCentralityToZero() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsCentrality()).thenReturn(false);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).containsEntry("centrality", 0.0);
    }

    @Test
    void normalizeUserPreferences_allPreferencesSet_containsAllKeys() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(true);
        when(dto.isNeedsNightlife()).thenReturn(false);
        when(dto.isNeedsCentrality()).thenReturn(true);

        Map<String, Double> result = service.normalizeUserPreferences(dto);

        assertThat(result).containsKeys("kindergarten", "school", "university", "bar", "nightclub", "centrality");
    }

    // --- normalizeUserWeights ---

    @Test
    void normalizeUserWeights_familyTrueWithRatings_convertsRatingsToWeights() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(true);
        when(dto.getKindergartenRating()).thenReturn(5);
        when(dto.getSchoolRating()).thenReturn(2);
        when(dto.getUniversityRating()).thenReturn(4);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("kindergarten")).isCloseTo(1.0, within(0.001));
        assertThat(result.get("school")).isCloseTo(0.4, within(0.001));
        assertThat(result.get("university")).isCloseTo(0.8, within(0.001));
    }

    @Test
    void normalizeUserWeights_familyFalse_usesDefaultExclusionWeightForAll() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(false);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("kindergarten")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("school")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("university")).isCloseTo(0.6, within(0.001));
    }

    @Test
    void normalizeUserWeights_nullRating_defaultsToThreeFifths() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(true);
        when(dto.getKindergartenRating()).thenReturn(null);
        when(dto.getSchoolRating()).thenReturn(null);
        when(dto.getUniversityRating()).thenReturn(null);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("kindergarten")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("school")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("university")).isCloseTo(0.6, within(0.001));
    }

    @Test
    void normalizeUserWeights_nightlifeTrueWithRatings_convertsRatings() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsNightlife()).thenReturn(true);
        when(dto.getBarRating()).thenReturn(3);
        when(dto.getClubRating()).thenReturn(1);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("bar")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("nightclub")).isCloseTo(0.2, within(0.001));
    }

    @Test
    void normalizeUserWeights_nightlifeFalse_usesDefaultExclusionWeight() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsNightlife()).thenReturn(false);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("bar")).isCloseTo(0.6, within(0.001));
        assertThat(result.get("nightclub")).isCloseTo(0.6, within(0.001));
    }

    @Test
    void normalizeUserWeights_centralityTrueWithRating_convertsRating() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsCentrality()).thenReturn(true);
        when(dto.getCentralityRating()).thenReturn(5);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result.get("centrality")).isCloseTo(1.0, within(0.001));
    }

    @Test
    void normalizeUserWeights_allNull_returnsEmptyMap() {
        MatchFormRequestDTO dto = mock(MatchFormRequestDTO.class);
        when(dto.isNeedsFamilyInstitutes()).thenReturn(null);
        when(dto.isNeedsNightlife()).thenReturn(null);
        when(dto.isNeedsCentrality()).thenReturn(null);

        Map<String, Double> result = service.normalizeUserWeights(dto);

        assertThat(result).isEmpty();
    }

    // --- sortPointsOfInterest ---

    @Test
    void sortPointsOfInterest_kindergartenType_goesToKindergarten() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of(makePoi("kindergarten")));

        assertThat(result.get("kindergarten")).hasSize(1);
    }

    @Test
    void sortPointsOfInterest_childcareType_goesToKindergartenBucket() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of(makePoi("childcare")));

        assertThat(result.get("kindergarten")).hasSize(1);
        assertThat(result).doesNotContainKey("childcare");
    }

    @Test
    void sortPointsOfInterest_collegeType_goesToUniversityBucket() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of(makePoi("college")));

        assertThat(result.get("university")).hasSize(1);
        assertThat(result).doesNotContainKey("college");
    }

    @Test
    void sortPointsOfInterest_universityType_goesToUniversity() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of(makePoi("university")));

        assertThat(result.get("university")).hasSize(1);
    }

    @Test
    void sortPointsOfInterest_barType_keepsOwnKey() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of(makePoi("bar")));

        assertThat(result.get("bar")).hasSize(1);
    }

    @Test
    void sortPointsOfInterest_mixedTypes_groupedCorrectly() {
        List<PointOfInterest> pois = List.of(
                makePoi("kindergarten"),
                makePoi("childcare"),
                makePoi("university"),
                makePoi("college"),
                makePoi("school"),
                makePoi("bar")
        );

        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(pois);

        assertThat(result.get("kindergarten")).hasSize(2);
        assertThat(result.get("university")).hasSize(2);
        assertThat(result.get("school")).hasSize(1);
        assertThat(result.get("bar")).hasSize(1);
    }

    @Test
    void sortPointsOfInterest_emptyList_returnsEmptyMap() {
        Map<String, List<PointOfInterest>> result = service.sortPointsOfInterest(List.of());

        assertThat(result).isEmpty();
    }

    // --- computeMatchingScore ---

    @Test
    void computeMatchingScore_perfectMatch_returns100() {
        Map<String, Double> targets = Map.of("kindergarten", 1.0, "bar", 0.0);
        Map<String, Double> weights = Map.of("kindergarten", 1.0, "bar", 0.6);
        Map<String, Double> normData = Map.of("kindergarten", 1.0, "bar", 0.0);

        double score = service.computeMatchingScore(targets, weights, normData);

        assertThat(score).isCloseTo(100.0, within(0.001));
    }

    @Test
    void computeMatchingScore_emptyTargets_returnsZero() {
        double score = service.computeMatchingScore(Map.of(), Map.of(), Map.of());

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void computeMatchingScore_worstMatch_returnsZero() {
        Map<String, Double> targets = Map.of("kindergarten", 1.0);
        Map<String, Double> weights = Map.of("kindergarten", 1.0);
        Map<String, Double> normData = Map.of("kindergarten", 0.0);

        double score = service.computeMatchingScore(targets, weights, normData);

        assertThat(score).isCloseTo(0.0, within(0.001));
    }

    @Test
    void computeMatchingScore_targetZeroActualZero_perfectScore() {
        // normData missing key → actual defaults to 0.0; target is 0.0 → perfect match
        Map<String, Double> targets = Map.of("bar", 0.0);
        Map<String, Double> weights = Map.of("bar", 1.0);

        double score = service.computeMatchingScore(targets, weights, Map.of());

        assertThat(score).isCloseTo(100.0, within(0.001));
    }

    @Test
    void computeMatchingScore_weightedAverage_calculatedCorrectly() {
        // key1: target=1, weight=2, actual=1 → 2*(1-0) = 2
        // key2: target=0, weight=1, actual=1 → 1*(1-1) = 0
        // score = 2/3 * 100 ≈ 66.67
        Map<String, Double> targets = Map.of("kindergarten", 1.0, "bar", 0.0);
        Map<String, Double> weights = Map.of("kindergarten", 2.0, "bar", 1.0);
        Map<String, Double> normData = Map.of("kindergarten", 1.0, "bar", 1.0);

        double score = service.computeMatchingScore(targets, weights, normData);

        assertThat(score).isCloseTo(200.0 / 3.0, within(0.001));
    }

    @Test
    void computeMatchingScore_missingWeightForKey_defaultsToOne() {
        Map<String, Double> targets = Map.of("kindergarten", 1.0);
        Map<String, Double> weights = Map.of(); // missing → defaults to 1.0
        Map<String, Double> normData = Map.of("kindergarten", 1.0);

        double score = service.computeMatchingScore(targets, weights, normData);

        assertThat(score).isCloseTo(100.0, within(0.001));
    }

    // --- computeMedianDensities ---

    @Test
    void computeMedianDensities_oddNumberOfDistricts_returnsMiddleValue() {
        // bar densities: [1/1, 2/1, 3/1] = [1.0, 2.0, 3.0] → median = 2.0
        Geometry square = unitSquare(0, 0);
        District d1 = mockDistrict(square, List.of(makePoi("bar")));
        District d2 = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar")));
        District d3 = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar"), makePoi("bar")));

        Map<String, Double> medians = service.computeMedianDensities(List.of(d1, d2, d3));

        assertThat(medians.get("bar")).isCloseTo(2.0, within(0.001));
    }

    @Test
    void computeMedianDensities_evenNumberOfDistricts_averagesMiddleTwo() {
        // bar densities: [1.0, 2.0, 3.0, 4.0] → median = (2+3)/2 = 2.5
        Geometry square = unitSquare(0, 0);
        District d1 = mockDistrict(square, List.of(makePoi("bar")));
        District d2 = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar")));
        District d3 = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar"), makePoi("bar")));
        District d4 = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar"), makePoi("bar"), makePoi("bar")));

        Map<String, Double> medians = service.computeMedianDensities(List.of(d1, d2, d3, d4));

        assertThat(medians.get("bar")).isCloseTo(2.5, within(0.001));
    }

    @Test
    void computeMedianDensities_noPoisOfType_medianIsZero() {
        Geometry square = unitSquare(0, 0);
        District d1 = mockDistrict(square, List.of());
        District d2 = mockDistrict(square, List.of());
        District d3 = mockDistrict(square, List.of());

        Map<String, Double> medians = service.computeMedianDensities(List.of(d1, d2, d3));

        assertThat(medians.get("bar")).isCloseTo(0.0, within(0.001));
        assertThat(medians.get("school")).isCloseTo(0.0, within(0.001));
    }

    // --- getNormalizedDistrictData ---

    @Test
    void getNormalizedDistrictData_densityBelowThreshold_returnsFractionalValue() {
        // 2 bars in area=1 → density=2; medianDensity=1 → threshold=4 → normalized=2/4=0.5
        Geometry square = unitSquare(0, 0);
        District district = mockDistrict(square, List.of(makePoi("bar"), makePoi("bar")));
        Map<String, Double> medians = zeroMedians("bar", 1.0);

        Map<String, Double> result = service.getNormalizedDistrictData(district, medians);

        assertThat(result.get("bar")).isCloseTo(0.5, within(0.001));
    }

    @Test
    void getNormalizedDistrictData_densityExceedsThreshold_clampedToOne() {
        // 10 bars in area=1 → density=10; threshold=4 → normalized=min(10/4,1)=1.0
        Geometry square = unitSquare(0, 0);
        District district = mockDistrict(square, makePois("bar", 10));
        Map<String, Double> medians = zeroMedians("bar", 1.0);

        Map<String, Double> result = service.getNormalizedDistrictData(district, medians);

        assertThat(result.get("bar")).isCloseTo(1.0, within(0.001));
    }

    @Test
    void getNormalizedDistrictData_zeroMedian_returnsOne() {
        // threshold = 0*4 = 0 → special case returns 1.0
        Geometry square = unitSquare(0, 0);
        District district = mockDistrict(square, List.of());
        Map<String, Double> medians = zeroMedians();

        Map<String, Double> result = service.getNormalizedDistrictData(district, medians);

        assertThat(result.get("bar")).isCloseTo(1.0, within(0.001));
    }

    // --- computeNormalizedCentralities ---

    @Test
    void computeNormalizedCentralities_districtAtCityCenter_getsHighestScore() {
        // 3 non-overlapping 1x1 squares in a row; middle one is closest to the union centroid
        District left   = mockDistrictWithGeometry("left",   unitSquare(0, 0));
        District center = mockDistrictWithGeometry("center", unitSquare(1, 0));
        District right  = mockDistrictWithGeometry("right",  unitSquare(2, 0));

        Map<String, Double> result = service.computeNormalizedCentralities(List.of(left, center, right));

        assertThat(result.get("center")).isCloseTo(1.0, within(0.001));
        assertThat(result.get("center")).isGreaterThan(result.get("left"));
        assertThat(result.get("center")).isGreaterThan(result.get("right"));
    }

    @Test
    void computeNormalizedCentralities_symmetricDistricts_outerOnesEqualScore() {
        District left   = mockDistrictWithGeometry("left",   unitSquare(0, 0));
        District center = mockDistrictWithGeometry("center", unitSquare(1, 0));
        District right  = mockDistrictWithGeometry("right",  unitSquare(2, 0));

        Map<String, Double> result = service.computeNormalizedCentralities(List.of(left, center, right));

        assertThat(result.get("left")).isCloseTo(result.get("right"), within(0.001));
    }

    @Test
    void computeNormalizedCentralities_allValuesInZeroToOneRange() {
        District left   = mockDistrictWithGeometry("left",   unitSquare(0, 0));
        District center = mockDistrictWithGeometry("center", unitSquare(1, 0));
        District right  = mockDistrictWithGeometry("right",  unitSquare(2, 0));

        Map<String, Double> result = service.computeNormalizedCentralities(List.of(left, center, right));

        result.values().forEach(v -> assertThat(v).isBetween(0.0, 1.0));
    }

    // --- helpers ---

    private PointOfInterest makePoi(String typeName) {
        PointOfInterest poi = mock(PointOfInterest.class);
        when(poi.getType()).thenReturn(new PoiType(typeName));
        return poi;
    }

    private List<PointOfInterest> makePois(String typeName, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> makePoi(typeName))
                .toList();
    }

    private District mockDistrict(Geometry polygon, List<PointOfInterest> pois) {
        District district = mock(District.class);
        when(district.getPolygon()).thenReturn(polygon);
        when(district.getPoi()).thenReturn(pois);
        return district;
    }

    private District mockDistrictWithGeometry(String name, Geometry polygon) {
        District district = mockDistrict(polygon, List.of());
        when(district.getName()).thenReturn(name);
        return district;
    }

    private Geometry unitSquare(double x, double y) {
        Coordinate[] coords = {
                new Coordinate(x, y),
                new Coordinate(x + 1, y),
                new Coordinate(x + 1, y + 1),
                new Coordinate(x, y + 1),
                new Coordinate(x, y)
        };
        return gf.createPolygon(coords);
    }

    private Map<String, Double> zeroMedians(String overrideKey, double overrideValue) {
        Map<String, Double> m = new java.util.HashMap<>();
        for (String key : List.of("kindergarten", "school", "university", "bar", "nightclub")) {
            m.put(key, key.equals(overrideKey) ? overrideValue : 0.0);
        }
        return m;
    }

    private Map<String, Double> zeroMedians() {
        return zeroMedians("__none__", 0.0);
    }
}
