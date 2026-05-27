package com.staysafe.application.controller.pointofinterest;

import com.staysafe.domain.district.District;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.pointofinterest.PoiType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

@Schema(description = "A single Point of Interest (POI) with its location and address details")
public class PoiResponseDTO {

    @Schema(description = "Internal database ID", example = "1")
    private final long id;

    @Schema(description = "OpenStreetMap node/way ID", example = "123456789")
    private final long osmId;

    @Schema(description = "Name of the POI as recorded in OSM", example = "Kita Sonnenschein")
    private final String name;

    @Schema(description = "POI type name (e.g. bar, kindergarten, school)", example = "kindergarten")
    private final String type;

    @Schema(description = "Name of the Hamburg district the POI belongs to", example = "Altona")
    private final String district;

    @Schema(description = "Postal code", example = "22765")
    private final String zipCode;

    @Schema(description = "Street name", example = "Große Bergstraße")
    private final String street;

    @Schema(description = "House number", example = "12")
    private final String houseNumber;

    @Schema(description = "WGS84 latitude", example = "53.5511")
    private final Double latitude;

    @Schema(description = "WGS84 longitude", example = "9.9937")
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
