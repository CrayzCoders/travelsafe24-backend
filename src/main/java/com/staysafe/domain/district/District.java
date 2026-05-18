package com.staysafe.domain.district;

import com.staysafe.domain.pointofinterest.PointOfInterest;
import com.staysafe.domain.city.City;
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

    @Column(columnDefinition = "geometry(MultiPolygon,4326)", nullable = false)
    private Geometry polygon;

    @OneToMany(mappedBy = "district",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PointOfInterest> poi;

    public District() {}

    public District(String name, City city, Geometry polygon) {
        this.name = name;
        this.city = city;
        this.polygon = polygon;
    }

    public List<PointOfInterest> getPoi() {
        return poi;
    }

    public Geometry getPolygon() {
        return polygon;
    }

    public String getName() {
        return name;
    }
}
