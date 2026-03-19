package com.staysafe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeometryDTO {
    private String type;
    private Object coordinates;

    public GeometryDTO() {
    }

    public GeometryDTO(String type, Object coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public Object getCoordinates() {
        return coordinates;
    }
}
