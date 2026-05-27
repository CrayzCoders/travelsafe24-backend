package com.staysafe.application.console.amenities;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.staysafe.domain.district.District;
import com.staysafe.domain.district.DistrictService;
import com.staysafe.domain.osmimport.OsmRoles;
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
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
@Profile("import-amenities")
public class LoadAmenities implements ApplicationRunner {
    private final GeometryFactory geometryFactory;
    private final KdTree savedNodesIndex;
    private final Map<Long, Coordinate> nodeIndex = new HashMap<>();
    private final Map<Long, Way> wayIndex = new HashMap<>();
    private final PoiService poiService;
    private final DistrictService districtService;
    private final GeometryBuilder builder;

    public LoadAmenities(PoiService poiService, DistrictService districtService) {
        this.poiService = poiService;
        this.districtService = districtService;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.savedNodesIndex = new KdTree();
        this.builder = new GeometryBuilder(geometryFactory);
        // TODO: initialize saveNodesIndex with existing Nodes, ways and relations in database
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws IOException {
        InputStream input = new ClassPathResource("import_osm_data/hamburg-260506.osm.pbf").getInputStream();
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
                    break;
                default:
                    break;
            }
        }

        List<Node> nodesToSave = getNodesWithAmenities(nodes);
        saveNodes(nodesToSave);

        List<Way> waysToBeSaved = getNeededWaysWithAmenities(ways);
        saveWays(waysToBeSaved);

        List<Relation> relationsToBeSaved = getNeededRelationsWithAmenities(relations);
        saveRelations(relationsToBeSaved);
    }

    private List<Node> getNodesWithAmenities(List<Node> nodes) {
        List<Node> nodesWithAmenities = new ArrayList<>();
        for (Node node : nodes) {
            List<? extends OsmTag> tags = node.getTags();
            for (OsmTag tag : tags) {
                if (OsmTags.AMENITY.getKeyValue().equals(tag.getKey()) && !tag.getValue().isEmpty()) {
                    nodesWithAmenities.add(node);
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
            if (!doesWayContainSavedNode(way)) {
                neededWaysWithAmenities.add(way);
            }
        }

        return neededWaysWithAmenities;
    }

    private List<Relation> getNeededRelationsWithAmenities(List<Relation> relations) {
        List<Relation> neededRelationsWithAmenities = new ArrayList<>();
        int relationsWithAmenityCount = 0;
        for (Relation relation : relations) {
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), relation.getTags());
            if (amenity == null) {
                continue;
            }
            relationsWithAmenityCount++;

            if (existsNodeOrWayInRelation(relation)) {
                continue;
            }

            neededRelationsWithAmenities.add(relation);
        }
        System.out.println("with amenities: " + relationsWithAmenityCount);
        System.out.println("unique: " + neededRelationsWithAmenities.size());

        return neededRelationsWithAmenities;
    }

    private boolean doesWayContainSavedNode(Way way) {
        Polygon geom = getWayPolygon(way);
        if (geom == null) {
            return true;
        }

        boolean containsSavedNode = false;

        @SuppressWarnings("unchecked")
        List<KdNode> candidates = savedNodesIndex.query(geom.getEnvelopeInternal());
        for (KdNode kdNode : candidates) {
            Point p = (Point) kdNode.getData();
            if (geom.contains(p)) {
                containsSavedNode = true;
                break;
            }
        }

        return containsSavedNode;
    }

    private boolean existsNodeOrWayInRelation(Relation relation) {
        for (OsmRelationMember member: relation.getMembers()) {
            if (Objects.requireNonNull(member.getType()) != EntityType.Way) {
                continue;
            }
            if (!member.getRole().equals(OsmRoles.OUTER.getKeyValue())) {
                continue;
            }

            Way way = wayIndex.get(member.getId());
            if (doesWayContainSavedNode(way)) {
                return true;
            }

            Polygon polygon = getWayPolygon(way);
            if (polygon == null) {
                continue;
            }

            List<PointOfInterest> existingPointInPolygon = poiService.findInsidePolygon(polygon);
            if (!existingPointInPolygon.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void saveNodes(List<Node> nodes) {
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
            savedNodesIndex.insert(point.getCoordinate(), point);
        }
    }

    private void saveWays(List<Way> ways) {
        for (Way way : ways) {
            List<? extends OsmTag> tags = way.getTags();
            String amenity = getTagValue(OsmTags.AMENITY.getKeyValue(), tags);
            if (null == amenity) {
                continue;
            }

            Polygon polygon = getWayPolygon(way);
            if (polygon == null) {
                continue;
            }

            Point center = polygon.getCentroid();

            if (center == null || center.isEmpty() || !center.isValid()) {
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
            savedNodesIndex.insert(center.getCoordinate(), center);
        }
    }

    private void saveRelations(List<Relation> relations) {
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

                Optional<PointOfInterest> existingRelation = poiService.findByOsmId(relation.getId());
                if (existingRelation.isPresent()) {
                    continue;
                }

                PoiType type = poiService.findOrCreateType(amenity);

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

    private Polygon getWayPolygon(Way way) {
        if (way == null) {
            return null;
        }

        Coordinate[] coords = getWayCoordinates(way);
        if (coords.length < 2 || !coords[0].equals2D(coords[coords.length - 1])) {
            return null;
        }

        LinearRing shell = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(shell);
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
