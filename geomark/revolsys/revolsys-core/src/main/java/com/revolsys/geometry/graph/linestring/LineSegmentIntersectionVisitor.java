package com.revolsys.geometry.graph.linestring;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.visitor.AbstractEdgeListenerVisitor;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.segment.LineSegment;

public class LineSegmentIntersectionVisitor extends AbstractEdgeListenerVisitor<LineSegment> {

  private final Set<Geometry> intersections = new LinkedHashSet<>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  @Override
  public void accept(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    if (lineSegment.getBoundingBox().bboxIntersects(this.querySeg.getBoundingBox())) {
      final Geometry intersection = this.querySeg.getIntersection(lineSegment);
      if (intersection != null && !intersection.isEmpty()) {
        this.intersections.add(intersection);
      }
    }
  }

  public Set<Geometry> getIntersections() {
    return this.intersections;
  }
}
