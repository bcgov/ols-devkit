package com.revolsys.geometry.graph.visitor;

import org.jeometry.common.math.Angle;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.EdgeVisitor;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;

public class NearParallelEdgeVisitor<T> extends EdgeVisitor<T> {

  private final LineString line;

  private final double maxDistance;

  public NearParallelEdgeVisitor(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public void accept(final Edge<T> edge) {
    final LineString matchLine = edge.getLineString();
    if (isAlmostParallel(matchLine)) {
      super.accept(edge);
    }
  }

  @Override
  public BoundingBox getEnvelope() {
    final BoundingBox envelope = this.line.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(this.maxDistance);
    return envelope;
  }

  private boolean isAlmostParallel(final LineString matchLine) {
    if (this.line.bboxDistance(matchLine) > this.maxDistance) {
      return false;
    }
    final LineString coords = this.line;
    final LineString matchCoords = this.line;
    Point previousCoordinate = coords.getPoint(0);
    for (int i = 1; i < coords.getVertexCount(); i++) {
      final Point coordinate = coords.getPoint(i);
      Point previousMatchCoordinate = matchCoords.getPoint(0);
      for (int j = 1; j < coords.getVertexCount(); j++) {
        final Point matchCoordinate = matchCoords.getPoint(i);
        final double distance = LineSegmentUtil.distanceLineLine(previousCoordinate, coordinate,
          previousMatchCoordinate, matchCoordinate);
        if (distance <= this.maxDistance) {
          final double angle1 = Angle.normalizePositive(previousCoordinate.angle2d(coordinate));
          final double angle2 = Angle
            .normalizePositive(previousMatchCoordinate.angle2d(matchCoordinate));
          final double angleDiff = Math.abs(angle1 - angle2);
          if (angleDiff <= Math.PI / 6) {
            return true;
          }
        }
        previousMatchCoordinate = matchCoordinate;
      }
      previousCoordinate = coordinate;
    }
    return false;
  }
}
