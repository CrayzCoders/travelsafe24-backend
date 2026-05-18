package com.staysafe.application.console.cities;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoJsonDistrictPropertiesDTO {
    @JsonProperty("Stadtteil")
    private String district;

    public GeoJsonDistrictPropertiesDTO() {
    }

    public GeoJsonDistrictPropertiesDTO(String district) {
        this.district = district;
    }

    public String getDistrict() {
        return district;
    }
}
