package com.revolsys.geometry.algorithm.locate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.Consumer4Double;

import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.index.intervalrtree.SortedPackedIntervalRTree;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleXY;

public class GeometrySegmentYIntervalIndex {

  private final SortedPackedIntervalRTree<LineSegmentDoubleXY> index;

  public GeometrySegmentYIntervalIndex(final Geometry geometry) {
    this(new SortedPackedIntervalRTree<>());
    geometry.forEachSegment((x1, y1, x2, y2) -> {
      final LineSegmentDoubleXY segment = new LineSegmentDoubleXY(x1, y1, x2, y2);
      if (y1 <= y2) {
        this.index.insert(y1, y2, segment);
      } else {
        this.index.insert(y2, y1, segment);
      }
    });
  }

  public GeometrySegmentYIntervalIndex(final SortedPackedIntervalRTree<LineSegmentDoubleXY> index) {
    this.index = index;
  }

  public List<LineSegment> getSegments(final double y) {
    final List<LineSegment> segments = new ArrayList<>();
    this.index.query(y, y, segments::add);
    return segments;
  }

  public boolean isIntersects(final RayCrossingCounter counter, final double x, final double y) {
    counter.reset(x, y);
    query(y, counter);
    return counter.isIntersects();
  }

  public void query(final double y, final Consumer<LineSegment> visitor) {
    this.index.query(y, y, visitor);
  }

  public void query(final double y, final Consumer4Double visitor) {
    this.index.query(y, y, segment -> {
      final double x1 = segment.getX1();
      final double y1 = segment.getY1();
      final double x2 = segment.getX2();
      final double y2 = segment.getY2();
      visitor.accept(x1, y1, x2, y2);
    });
  }

  public void query(final double minY, final double maxY, final Consumer<LineSegment> visitor) {
    this.index.query(minY, maxY, visitor);
  }

  public void query(final double minY, final double maxY, final Consumer4Double visitor) {
    this.index.query(minY, maxY, segment -> {
      final double x1 = segment.getX1();
      final double y1 = segment.getY1();
      final double x2 = segment.getX2();
      final double y2 = segment.getY2();
      visitor.accept(x1, y1, x2, y2);
    });
  }
}
