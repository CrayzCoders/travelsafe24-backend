package com.staysafe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OverpassResponseDTO {
    private List<Element> elements;

    public List<Element> getElements() { return elements; }
    public void setElements(List<Element> elements) { this.elements = elements; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {
        private String type;
        private long id;
        private Double lat;
        private Double lon;
        private Center center;
        private Tags tags;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
        public Center getCenter() { return center; }
        public void setCenter(Center center) { this.center = center; }
        public Tags getTags() { return tags; }
        public void setTags(Tags tags) { this.tags = tags; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Center {
        private Double lat;
        private Double lon;
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tags {
        private String name;
        private String amenity;

        @JsonProperty("addr:city")
        private String city;

        @JsonProperty("addr:postcode")
        private String zip;

        @JsonProperty("addr:street")
        private String street;

        @JsonProperty("addr:housenumber")
        private String houseNumber;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAmenity() { return amenity; }
        public void setAmenity(String amenity) { this.amenity = amenity; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getHouseNumber() { return houseNumber; }
        public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
    }
}
