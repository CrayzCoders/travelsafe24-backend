package com.staysafe.dto;

public class CriteriaDTO {
    private final String name;
    private final double value;

    public CriteriaDTO(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
