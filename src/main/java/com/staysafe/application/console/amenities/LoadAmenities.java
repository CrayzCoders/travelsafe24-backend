package com.staysafe.application.console.amenities;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.staysafe.domain.district.District;
import com.staysafe.domain.district.DistrictService;
import com.staysafe.domain.osmimport.OsmTags;
import com.staysafe.domain.pointofinterest.PoiService;
import com.staysafe.domain.pointofinterest.PoiType;
import com.staysafe.domain.pointofinterest.PointOfInterest;
import de.topobyte.osm4j.core.model.iface.*;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import org.jspecify.annotations.NonNull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

@Component
@Profile("import-amenities")
public class LoadAmenities implements ApplicationRunner {
    private final GeometryFactory geometryFactory;
    private final STRtree index;
    private final Map<Long, Coordinate> nodeIndex = new HashMap<>();
    private final Map<Long, Way> wayIndex = new HashMap<>();
    private final PoiService poiService;
    private final DistrictService districtService;
    private final GeometryBuilder builder;

    public LoadAmenities(PoiService poiService, DistrictService districtService) {
        this.poiService = poiService;
        this.districtService = districtService;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.index = new STRtree();
        this.builder = new GeometryBuilder(geometryFactory);
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws FileNotFoundException {
        InputStream input = new FileInputStream("src/main/resources/import_osm_data/hamburg-260506.osm.pbf");
        PbfIterator iterator = new PbfIterator(input, true);

        List<Node> nodes = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        for (EntityContainer container : iterator) {
            switch (container.getType()) {
                case Node:
                    Node node = (Node) container.getEntity();
                    nodeIndex.put(
                            node.getId(),
                            new Coordinate(node.getLongitude(), node.getLatitude())
                    );
                    nodes.add(node);
                    break;
                case Way:
                    Way way = (Way) container.getEntity();
                    wayIndex.put(
                            way.getId(),
                            way
                    );
                    ways.add(way);
                    break;
                case Relation:
                    relations.add((Relation) container.getEntity());
                default:
                    break;
            }
        }

        List<Node> nodesToSafe = getNodesWithAmenities(nodes);
        List<Way> waysToBeSafed = getNeededWaysWithAmenities(ways);
        List<Relation> relationsToBeSaved = getNeededRelationsWithAmenities(relations, waysToBeSafed, nodesToSafe);

        safeNodes(nodesToSafe);
        safeWays(waysToBeSafed);
        safeRelations(relationsToBeSaved);
    }

    private List<Node> getNodesWithAmenities(List<Node> nodes) {
        List<Node> nodesWithAmenities = new ArrayList<>();
        for (Node node : nodes) {
            List<? extends OsmTag> tags = node.getTags();
            for (OsmTag tag : tags) {
                if (OsmTags.AMENITY.getKeyValue().equals(tag.getKey()) && !tag.getValue().isEmpty()) {
                    nodesWithAmenities.add(node);
                    Point p = builder.build(node);
                    index.insert(p.getEnvelopeInternal(), p);
                }
            }
        }

        return nodesWithAmenities;
    }

    private List<Way> getNeededWaysWithAmenities(List<Way> ways) {
        List<Way> waysWithAmenities = new ArrayList<>();

        // only get ways with amenities set
        for (Way way : ways) {
            List<? extends OsmTag> tags = way.getTags();
            for (OsmTag tag : tags) {
                if (OsmTags.AMENITY.getKeyValue().equals(tag.getKey()) && !tag.getValue().isEmpty()) {
                    waysWithAmenities.add(way);
                }
            }
        }

        List<Way> neededWaysWithAmenities = new ArrayList<>();
        // check for nodes
        for (Way way : waysWithAmenities) {
            Coordinate[] coords = getWayCoordinates(way);

            // return the correct geometry. whether the way is closed or not (i.e. starting point = end point)
            if (coords.length < 2 || !coords[0].equals2D(coords[coords.length - 1])) {
                continue;
            }
            Geometry geom = geometryFactory.createPolygon(coords);

            boolean containsAmenity = false;

            @SuppressWarnings("unchecked")
            List<Point> candidates = index.query(geom.getEnvelopeInternal());
            // check whether one valid node (point of interest) lies within the boundary of the way
            for (Point p : candidates) {
                if (geom.contains(p)) {
                    containsAmenity = true;
                    break;
                }
            }

            if (!containsAmenity) {
                neededWaysWithAmenities.add(way);
            }
        }

        return neededWaysWithAmenities;
    }

    private List<Relation> getNeededRelationsWithAmenities(List<Relation> relations, List<Way> savedWays, List<Node> savedNodes) {
        List<Relation> neededRelationsWithAmenities = new ArrayList<>();
        for (Relation relation : relations) {
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), relation.getTags());
            if (amenity == null) {
                continue;
            }

            if (existsNodeOrWayInRelation(relation, savedWays, savedNodes)) {
                continue;
            }

            neededRelationsWithAmenities.add(relation);
        }

        return neededRelationsWithAmenities;
    }

    private boolean existsNodeOrWayInRelation(Relation relation, List<Way> ways, List<Node> nodes) {
        for (OsmRelationMember member: relation.getMembers()) {
            switch (member.getType()) {
                case Node:
                    if (nodes.stream().anyMatch((node) -> node.getId() == member.getId())) {
                        return true;
                    }
                    break;
                case Way:
                    if (ways.stream().anyMatch((way) -> way.getId() == member.getId())) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    private void safeNodes(List<Node> nodes) {
        for (Node node: nodes) {
            List<? extends OsmTag> tags = node.getTags();
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), tags);
            if (null == amenity) {
                continue;
            }

            Point point = builder.build(node);
            Optional<District> districtOptional = districtService.findByPoint(point);
            if (districtOptional.isEmpty()) {
                continue;
            }

            Optional<PointOfInterest> existingPoi = poiService.findByOsmId(node.getId());
            if (existingPoi.isPresent()) {
                continue;
            }

            District district = districtOptional.get();
            PoiType type = poiService.findOrCreateType(amenity);
            String name = getTagValue(OsmTags.NAME.getKeyValue(), tags);

            PointOfInterest poi = new PointOfInterest(
                    node.getId(),
                    name != null ? name : "",
                    point,
                    district,
                    type
            );
            poiService.savePointOfInterest(poi);
        }
    }

    private void safeWays(List<Way> ways) {
        for(Way way: ways) {
            List<? extends OsmTag> tags = way.getTags();
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), tags);
            if (null == amenity) {
                continue;
            }

            Coordinate[] coords = getWayCoordinates(way);
            LinearRing shell = geometryFactory.createLinearRing(coords);
            Polygon polygon = geometryFactory.createPolygon(shell);
            Point center = polygon.getCentroid();

            if(center.isEmpty() || !center.isValid()) {
                continue;
            }

            Optional<PointOfInterest> existingWay = poiService.findByOsmId(way.getId());
            if (existingWay.isPresent()) {
                continue;
            }

            PoiType type = poiService.findOrCreateType(amenity);

            List<PointOfInterest> existingPointInPolygon = poiService.findInsidePolygon(polygon);
            if (!existingPointInPolygon.isEmpty()) {
                continue;
            }

            Optional<District> districtOptional = districtService.findByPoint(center);
            if (districtOptional.isEmpty()) {
                continue;
            }
            District district = districtOptional.get();

            String name = getTagValue(OsmTags.NAME.getKeyValue(), tags);

            PointOfInterest poi = new PointOfInterest(
                    way.getId(),
                    name != null ? name : "",
                    center,
                    district,
                    type
            );
            poiService.savePointOfInterest(poi);
        }
    }

    private void safeRelations(List<Relation> relations) {
        for (Relation relation: relations) {
            List<? extends OsmTag> tags = relation.getTags();
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), tags);
            if (null == amenity) {
                continue;
            }

            try {
                Geometry geometry = builder.build(relation, new RelationProvider(relation));

                Point center = geometry.getInteriorPoint();
                if (center == null || center.isEmpty() || !center.isValid()) {
                    continue;
                }

                Optional<PointOfInterest> existingWay = poiService.findByOsmId(relation.getId());
                if (existingWay.isPresent()) {
                    continue;
                }

                PoiType type = poiService.findOrCreateType(amenity);

                List<PointOfInterest> existingPointInPolygon = poiService.findInsidePolygon((Polygon) geometry);
                if (!existingPointInPolygon.isEmpty()) {
                    continue;
                }

                Optional<District> districtOptional = districtService.findByPoint(center);
                if (districtOptional.isEmpty()) {
                    continue;
                }
                District district = districtOptional.get();

                String name = getTagValue(OsmTags.NAME.getKeyValue(), tags);

                PointOfInterest poi = new PointOfInterest(
                        relation.getId(),
                        name != null ? name : "",
                        center,
                        district,
                        type
                );
                poiService.savePointOfInterest(poi);
            } catch (Exception e) {
                // Relation nicht vollständig → skip
            }
        }
    }

    private String getTagValue(String amenityType, List<? extends OsmTag> tags) {
        String amenity = "";
        for (OsmTag tag: tags) {
            if (amenityType.equals(tag.getKey())) {
                amenity = tag.getValue();
            }
        }

        if (amenity.isEmpty()) {
            return null;
        }

        return amenity;
    }

    private Coordinate[] getWayCoordinates(Way way) {
        TLongList wayNodes = way.getNodes();

        Coordinate[] coords = new Coordinate[wayNodes.size()];
        // determine the valid coordinates of the way (i.e. the nodes which, when connected, results in the envelope of the building
        for (int i = 0; i < wayNodes.size(); i++) {
            long nodeId = wayNodes.get(i);

            Coordinate c = nodeIndex.get(nodeId);
            if (c != null) {
                coords[i] = c;
            }
        }

        return coords;
    }

    class RelationProvider implements OsmEntityProvider {
        private final Relation relation;

        RelationProvider(Relation relation) {
            this.relation = relation;
        }
        @Override
        public OsmNode getNode(long id) {
            Coordinate c = nodeIndex.get(id);
            if (c == null) throw new RuntimeException("missing node " + id);
            return new Node(id, c.x, c.y);
        }

        @Override
        public OsmWay getWay(long id) {
            Way way = wayIndex.get(id);

            if (way == null) throw new RuntimeException("missing way " + id);
            return way;
        }

        @Override
        public OsmRelation getRelation(long id) {
            return relation;
        }
    }
}
