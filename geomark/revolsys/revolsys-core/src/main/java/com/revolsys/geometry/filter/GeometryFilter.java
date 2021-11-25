package com.revolsys.geometry.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

public class GeometryFilter {
  public static <T extends Geometry> Predicate<T> intersects(final BoundingBox boundingBox) {
    return (geometry) -> {
      if (boundingBox == null || geometry == null) {
        return false;
      } else {
        final BoundingBox geometryBoundingBox = geometry.getBoundingBox();
        return boundingBox.bboxIntersects(geometryBoundingBox);
      }
    };
  }

  public static Predicate<LineString> lineContainedWithinTolerance(final LineString line,
    final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance, true);
  }

  public static Predicate<LineString> lineContainsWithinTolerance(final LineString line,
    final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance);
  }

  public static Predicate<LineString> lineWithinDistance(final LineString line,
    final double maxDistance) {
    return new LineStringLessThanDistanceFilter(line, maxDistance);
  }
}
