package com.staysafe.database.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "poi_type")
public class PoiType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "type",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PointOfInterest> poi;
}
