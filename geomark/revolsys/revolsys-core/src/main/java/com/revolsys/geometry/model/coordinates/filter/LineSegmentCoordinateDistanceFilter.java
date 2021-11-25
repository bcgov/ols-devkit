package com.revolsys.geometry.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;

public class LineSegmentCoordinateDistanceFilter implements Predicate<Segment> {

  private final double maxDistance;

  private final Point point;

  public LineSegmentCoordinateDistanceFilter(final Point point, final double maxDistance) {
    this.point = point;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final Segment lineSegment) {
    final double distance = lineSegment.distancePoint(this.point);
    if (distance < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
