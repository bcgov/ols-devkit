package com.revolsys.geometry.operation.buffer;

import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;

/**
 * A segment from a directed edge which has been assigned a depth value
 * for its sides.
 */
public class DepthSegment implements Comparable<DepthSegment> {
  private final int leftDepth;

  private final LineSegment upwardSeg;

  public DepthSegment(final LineSegment seg, final int depth) {
    // input seg is assumed to be normalized
    this.upwardSeg = new LineSegmentDouble(seg);
    // upwardSeg.normalize();
    this.leftDepth = depth;
  }

  /**
   * Defines a comparision operation on DepthSegments
   * which orders them left to right
   *
   * <pre>
   * DS1 < DS2   if   DS1.seg is left of DS2.seg
   * DS1 > DS2   if   DS1.seg is right of DS2.seg
   * </pre>
   *
   * @param obj
   * @return the comparison value
   */
  @Override
  public int compareTo(final DepthSegment other) {
    /**
     * try and compute a determinate orientation for the segments.
     * Test returns 1 if other is left of this (i.e. this > other)
     */
    int orientIndex = this.upwardSeg.orientationIndex(other.upwardSeg);

    /**
     * If comparison between this and other is indeterminate,
     * try the opposite call order.
     * orientationIndex value is 1 if this is left of other,
     * so have to flip sign to get proper comparison value of
     * -1 if this is leftmost
     */
    if (orientIndex == 0) {
      orientIndex = -1 * other.upwardSeg.orientationIndex(this.upwardSeg);
    }

    // if orientation is determinate, return it
    if (orientIndex != 0) {
      return orientIndex;
    }

    // otherwise, segs must be collinear - sort based on minimum X value
    return compareX(this.upwardSeg, other.upwardSeg);
  }

  /**
   * Compare two collinear segments for left-most ordering.
   * If segs are vertical, use vertical ordering for comparison.
   * If segs are equal, return 0.
   * Segments are assumed to be directed so that the second coordinate is >= to the first
   * (e.g. up and to the right).
   *
   * @param seg0 a segment to compare
   * @param seg1 a segment to compare
   * @return
   */
  private int compareX(final LineSegment seg0, final LineSegment seg1) {
    final int compare0 = seg0.getP0().compareTo(seg1.getP0());
    if (compare0 != 0) {
      return compare0;
    }
    return seg0.getP1().compareTo(seg1.getP1());

  }

  public int getLeftDepth() {
    return this.leftDepth;
  }

}
