package com.staysafe.application.controller.pointofinterest;

import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

public class PoiResponseDTO {
    private final long id;
    private final long osmId;
    private final String name;
    private final String type;
    private final String district;
    private final String zipCode;
    private final String street;
    private final String houseNumber;
    private final Double latitude;
    private final Double longitude;

    public PoiResponseDTO(long id, long osmId, String name, String type, String district, String zipCode, String street, String houseNumber, Double latitude, Double longitude) {
        this.id = id;
        this.osmId = osmId;
        this.name = name;
        this.type = type;
        this.district = district;
        this.zipCode = zipCode;
        this.street = street;
        this.houseNumber = houseNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public long getOsmId() {
        return osmId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDistrict() {
        return district;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public static PoiResponseDTO fromEntity(PointOfInterest poi) {
        PoiType poiType = poi.getType();
        String typeName = poiType != null ? poiType.getName() : null;
        District district = poi.getDistrict();
        String districtName = district != null ? district.getName() : null;

        Double latitude = null;
        Double longitude = null;
        Geometry location = poi.getLocation();
        if (location instanceof Point point) {
            latitude = point.getY();
            longitude = point.getX();
        }

        return new PoiResponseDTO(
            poi.getId(),
            poi.getOsmId(),
            poi.getName(),
            typeName,
            districtName,
            poi.getZipCode(),
            poi.getStreet(),
            poi.getHouseNumber(),
            latitude,
            longitude
        );
    }
}
