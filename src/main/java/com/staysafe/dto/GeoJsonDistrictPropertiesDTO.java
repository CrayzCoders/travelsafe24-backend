package com.staysafe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
