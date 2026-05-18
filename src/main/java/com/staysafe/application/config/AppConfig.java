package com.staysafe.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "staysafe")
public class AppConfig {
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private Map<String, String> urls;
    private List<String> areas;
    private List<String> amenities;
    private int amenityLimit;

    public String getOverpassUrl() {
        return urls.get("overpass");
    }

    public List<String> getAreas() {
        return areas;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public int getAmenityLimit() {
        return amenityLimit;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public void setAreas(List<String> areas) {
        this.areas = areas;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public void setAmenityLimit(int amenityLimit) {
        this.amenityLimit = amenityLimit;
    }
}
