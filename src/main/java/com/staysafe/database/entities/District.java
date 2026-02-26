package com.staysafe.database.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

@Entity
@Table(name = "districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = true)
    private int population;

    @Column(columnDefinition = "geometry(Polygon,4326)")
    private Geometry polygon;

    @OneToMany(mappedBy = "district",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PointOfInterest> poi;
}
