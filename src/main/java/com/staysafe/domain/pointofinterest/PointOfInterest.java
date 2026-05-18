package com.staysafe.domain.pointofinterest;

import com.staysafe.domain.district.District;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "point_of_interests")
public class PointOfInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false, name = "osm_id")
    private long osmId;

    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Geometry location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "zip_code")
    private String zipCode;

    @Column
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poi_type_id", nullable = false)
    private PoiType type;

    public PointOfInterest() {
    }

    public PointOfInterest(long osmId, String name, Geometry location, District district, PoiType type) {
        this.osmId = osmId;
        this.name = name;
        this.location = location;
        this.district = district;
        this.type = type;
    }

    public PointOfInterest(
            long osmId,
            String name,
            Geometry location,
            District district,
            String zipCode,
            String street,
            String houseNumber,
            PoiType type
    ) {
        this.osmId = osmId;
        this.name = name;
        this.location = location;
        this.district = district;
        this.zipCode = zipCode;
        this.street = street;
        this.houseNumber = houseNumber;
        this.type = type;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public PoiType getType() {
        return type;
    }
}
