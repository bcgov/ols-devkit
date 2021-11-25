package com.revolsys.geometry.index.quadtree;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.util.Property;

public class GeometrySegmentQuadTree extends IdObjectQuadTree<Segment> {

  private static final long serialVersionUID = 1L;

  public static GeometrySegmentQuadTree get(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      return new GeometrySegmentQuadTree(geometry);
    } else {
      return null;
    }
  }

  private final Geometry geometry;

  public GeometrySegmentQuadTree(final Geometry geometry) {
    super(geometry.getGeometryFactory());
    this.geometry = geometry;
    if (geometry != null) {
      setGeometryFactory(geometry.getGeometryFactory());
      for (final Segment segment : geometry.segments()) {
        final BoundingBox boundingBox = segment.getBoundingBox();
        insertItem(boundingBox, segment);
      }
    }
  }

  @Override
  protected Object getId(final Segment segment) {
    return segment.getSegmentId();
  }

  @Override
  protected Segment getItem(final Object id) {
    final int[] segmentId = (int[])id;
    return this.geometry.getSegment(segmentId);
  }

  public List<Segment> getWithinDistance(final Point point, final double maxDistance) {
    final BoundingBox boundingBox = point.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
    final LineSegmentCoordinateDistanceFilter filter = new LineSegmentCoordinateDistanceFilter(
      point, maxDistance);
    return getItems(boundingBox, filter);
  }

  @Override
  protected boolean intersectsBounds(final Object id, final double x, final double y) {
    final Segment segment = getItem(id);
    final BoundingBox boundingBox = segment.getBoundingBox();
    return boundingBox.bboxIntersects(x, y, x, y);
  }

  @Override
  protected boolean intersectsBounds(final Object id, final double minX, final double minY,
    final double maxX, final double maxY) {
    final Segment segment = getItem(id);
    final BoundingBox boundingBox = segment.getBoundingBox();
    return boundingBox.bboxIntersects(minX, minY, maxX, maxY);
  }

}
