package com.staysafe.services.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class GeometryUtils {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static Point createPoint(double lat, double lon) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        return point;
    }
}
