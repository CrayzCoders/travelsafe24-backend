package com.staysafe.database.entities;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @OneToMany(mappedBy = "city",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<District> districts;

    @Column(nullable = true)
    private int population;

    @Column(columnDefinition = "geometry(Polygon,4326)")
    private Geometry polygon;
}
