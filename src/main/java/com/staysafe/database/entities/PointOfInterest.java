package com.staysafe.database.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

@Entity
@Table(name = "point_of_interests")
public class PointOfInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "osm_id")
    private int osmId;

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
}
