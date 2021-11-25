package com.revolsys.geometry.algorithm.linematch;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class LineSegmentMatch {
  private final LineSegment segment;

  private final List<LineSegment> segments = new ArrayList<>();

  public LineSegmentMatch(final GeometryFactory geometryFactory, final Point start,
    final Point end) {
    this(new LineSegmentDoubleGF(geometryFactory, start, end));
  }

  public LineSegmentMatch(final LineSegment segment) {
    this.segment = segment;
  }

  public void addSegment(final LineSegment segment, final int index) {
    while (index >= this.segments.size()) {
      this.segments.add(null);
    }
    this.segments.set(index, segment);
  }

  public BoundingBox getEnvelope() {
    return this.segment.getBoundingBox();
  }

  public LineString getLine() {
    return this.segment;
  }

  public int getMatchCount(final int index) {
    if (this.segments.get(index) == null) {
      return 0;
    }
    int matchCount = 0;
    for (final LineSegment segment : this.segments) {
      if (segment != null) {
        matchCount++;
      }

    }
    return matchCount;
  }

  /**
   * @return the segment
   */
  public LineSegment getSegment() {
    return this.segment;
  }

  /**
   * @param index
   * @return the segment
   */
  public LineSegment getSegment(final int index) {
    return this.segments.get(index);
  }

  /**
   * @return the segments
   */
  public int getSegmentCount() {
    return this.segments.size();
  }

  /**
   * @return the segments
   */
  public List<LineSegment> getSegments() {
    return this.segments;
  }

  public boolean hasMatches(final int index) {
    if (index < this.segments.size()) {
      final LineSegment segment = this.segments.get(index);
      if (segment == null) {
        return false;
      }
      for (int i = 0; i < this.segments.size(); i++) {
        if (i != index) {
          final LineSegment otherSegment = this.segments.get(i);
          if (otherSegment != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean hasMatches(final int index1, final int index2) {
    if (index1 < this.segments.size() && index2 < this.segments.size()) {
      final LineSegment segment1 = this.segments.get(index1);
      final LineSegment segment2 = this.segments.get(index2);
      return segment1 != null && segment2 != null;
    } else {
      return false;
    }
  }

  public boolean hasOtherSegment(final int index) {
    for (int i = 0; i < this.segments.size(); i++) {
      if (i != index) {
        if (this.segments.get(i) != null) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasSegment(final int index) {
    if (index < this.segments.size()) {
      return this.segments.get(index) != null;
    } else {
      return false;
    }
  }

  public boolean isMatchedWithBase(final int index) {
    if (this.segments.get(index) == null) {
      return false;
    } else if (this.segments.get(0) == null) {
      return false;
    } else {
      return true;
    }
  }

  public void removeSegment(final int i) {
    this.segments.set(i, null);
  }

  @Override
  public String toString() {
    return this.segment.toString();
  }
}
