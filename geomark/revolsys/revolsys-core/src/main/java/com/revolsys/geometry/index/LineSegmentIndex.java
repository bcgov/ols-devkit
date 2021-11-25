package com.revolsys.geometry.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.index.visitor.LineSegmentIntersectionVisitor;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class LineSegmentIndex extends QuadTree<LineSegment> {
  private static final long serialVersionUID = 1L;

  public LineSegmentIndex(final Geometry geometry) {
    super(geometry.getGeometryFactory());
    insert(geometry);
  }

  public LineSegmentIndex(final Lineal line) {
    super(line.getGeometryFactory());
    insert(line);
  }

  public void insert(final Geometry geometry) {
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof LineString) {
        final LineString line = (LineString)subGeometry;
        insert(line);
      }
    }
  }

  public void insert(final Lineal line) {
    for (final LineSegment lineSegment : line.segments()) {
      final LineSegment clone = (LineSegment)lineSegment.clone();
      insert(clone);
    }
  }

  public void insert(final LineSegment lineSegment) {
    final BoundingBox envelope = lineSegment.getBoundingBox();
    insertItem(envelope, lineSegment);
  }

  public boolean isWithinDistance(final Point point) {
    final BoundingBox envelope = point.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(1);
    final List<LineSegment> lines = getItems(envelope);
    for (final LineSegment line : lines) {
      if (line.distancePoint(point) <= 1) {
        return true;
      }
    }

    return false;
  }

  public List<Geometry> queryIntersections(final LineSegment querySeg) {
    final BoundingBox env = querySeg.getBoundingBox();
    final LineSegmentIntersectionVisitor visitor = new LineSegmentIntersectionVisitor(querySeg);
    forEach(env, visitor);
    final List<Geometry> intersections = new ArrayList<>(visitor.getIntersections());
    return intersections;
  }

  public List<Geometry> queryIntersections(final Point c0, final Point c1) {
    return queryIntersections(new LineSegmentDoubleGF(c0, c1));
  }
}
