package com.revolsys.geometry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public class UniqueCoordinateArrayFilter {
  public static List<Point> getUniquePoints(final Geometry geometry) {
    final Iterable<Vertex> vertices = geometry.vertices();
    return getUniquePoints(vertices);
  }

  public static List<Point> getUniquePoints(final Iterable<? extends Point> coordinates) {
    final Set<Point> set = new TreeSet<>();
    final List<Point> points = new ArrayList<>();
    for (final Point point : coordinates) {
      if (!set.contains(point)) {
        final Point clone = point.newPoint2D();
        points.add(clone);
        set.add(clone);
      }
    }
    return points;
  }

  public static Point[] getUniquePointsArray(final Geometry geometry) {
    final List<Point> points = getUniquePoints(geometry);
    return points.toArray(new Point[points.size()]);
  }

  public static Point[] getUniquePointsArray(final Iterable<? extends Point> coordinates) {
    final List<Point> points = getUniquePoints(coordinates);
    return points.toArray(new Point[points.size()]);
  }

}
