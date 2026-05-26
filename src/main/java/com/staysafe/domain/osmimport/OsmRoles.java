package com.staysafe.domain.osmimport;

public enum OsmRoles {
    OUTER("outer"),
    INNER("inner");

    private final String keyValue;

    OsmRoles(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getKeyValue() {
        return keyValue;
    }
}
