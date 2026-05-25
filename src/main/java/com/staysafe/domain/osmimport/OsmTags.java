package com.staysafe.domain.osmimport;

public enum OsmTags {
    AMENITY("amenity"),
    NAME("name");

    private final String keyValue;

    OsmTags(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getKeyValue() {
        return keyValue;
    }
}
