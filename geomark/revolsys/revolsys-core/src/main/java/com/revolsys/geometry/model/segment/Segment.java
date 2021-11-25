package com.revolsys.geometry.model.segment;

import java.util.Iterator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public interface Segment
  extends LineSegment, Iterator<Segment>, Iterable<Segment>, GeometryComponent {
  @Override
  Segment clone();

  default int compareSegmentId(final Segment segment) {
    if (segment == null) {
      return 1;
    } else {
      final int[] segmentId1 = getSegmentId();
      final int[] segmentId2 = segment.getSegmentId();
      int compare = Integer.compare(segmentId1.length, segmentId2.length);
      if (compare == 0) {
        for (int i = 0; i < segmentId1.length; i++) {
          compare = Integer.compare(segmentId1[i], segmentId2[i]);
          if (compare != 0) {
            return compare;
          }
        }
      }
      return compare;
    }
  }

  <V extends Geometry> V getGeometry();

  Vertex getGeometryVertex(int index);

  default int getPartIndex() {
    return -1;
  }

  default int getRingIndex() {
    return -1;
  }

  int[] getSegmentId();

  default int getSegmentIndex() {
    final int[] segmentId = getSegmentId();
    return segmentId[segmentId.length - 1];
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isEndIntersection(final Point point) {
    if (isLineStart()) {
      return equalsVertex(2, 0, point);
    } else if (isLineEnd()) {
      return equalsVertex(2, 1, point);
    } else {
      return false;
    }
  }

  boolean isLineClosed();

  boolean isLineEnd();

  boolean isLineStart();

  @Override
  default Iterator<Segment> iterator() {
    return this;
  }

  void setSegmentId(int... segmentId);
}
