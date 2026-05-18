package com.staysafe.application.console.cities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeaturesDTO {
    private List<GeoJsonDistrictDTO> features;

    public FeaturesDTO(){
    }

    public List<GeoJsonDistrictDTO> getFeatures() {
        return features;
    }
}
