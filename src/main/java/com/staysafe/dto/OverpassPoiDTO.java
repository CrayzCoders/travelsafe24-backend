package com.staysafe.dto;

public record OverpassPoiDTO(long id, String overpassType, double lat, double lon, String name) {
    @Override
    public String name() {
        return (name != null) ? name : "";
    }
}
