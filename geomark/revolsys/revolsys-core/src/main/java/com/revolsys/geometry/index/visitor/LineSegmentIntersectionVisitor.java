package com.revolsys.geometry.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.segment.LineSegment;

public class LineSegmentIntersectionVisitor implements Consumer<LineSegment> {

  private final Set<Geometry> intersections = new LinkedHashSet<>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  @Override
  public void accept(final LineSegment segment) {
    if (segment.getBoundingBox().bboxIntersects(this.querySeg.getBoundingBox())) {
      final Geometry intersection = this.querySeg.getIntersection(segment);
      if (intersection != null && intersection.isEmpty()) {
        this.intersections.add(intersection);
      }
    }
  }

  public Set<Geometry> getIntersections() {
    return this.intersections;
  }
}
