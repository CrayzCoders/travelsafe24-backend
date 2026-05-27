package com.staysafe.domain.pointofinterest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.List;

@Schema(description = "A category/type of Point of Interest")
@Entity
@Table(name = "poi_type")
public class PoiType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Schema(description = "Type name (e.g. bar, kindergarten, school, nightclub, university)", example = "kindergarten")
    @Column(nullable = false, unique = true)
    private String name;

    @Schema(hidden = true)
    @OneToMany(mappedBy = "type",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PointOfInterest> poi;

    public PoiType() {
    }

    public PoiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}