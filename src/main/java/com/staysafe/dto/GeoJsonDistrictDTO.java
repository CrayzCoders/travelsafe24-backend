package com.staysafe.dto;

public class GeoJsonDistrictDTO {
    private GeoJsonDistrictPropertiesDTO properties;
    private GeometryDTO geometry;

    public GeoJsonDistrictDTO() {
    }

    public GeoJsonDistrictDTO(GeoJsonDistrictPropertiesDTO properties, GeometryDTO geometry) {
        this.properties = properties;
        this.geometry = geometry;
    }

    public  GeoJsonDistrictPropertiesDTO getProperties() {
        return properties;
    }

    public GeometryDTO getGeometry() {
        return geometry;
    }
}
