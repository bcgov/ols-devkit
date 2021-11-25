/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.elevation.tin.quadedge;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;

/**
 * A strategy for finding constraint split points which attempts to maximise the length of the split
 * segments while preventing further encroachment. (This is not always possible for narrow angles).
 *
 * @author Martin Davis
 */
public class NonEncroachingSplitPointFinder implements ConstraintSplitPointFinder {

  /**
   * Computes a split point which is the projection of the encroaching point on the segment
   *
   * @param seg
   * @param encroachPt
   * @return a split point on the segment
   */
  public static Point projectedSplitPoint(final LineSegmentDoubleData seg, final Point encroachPt) {
    final LineSegment lineSeg = seg;
    final Point projPt = lineSeg.project(encroachPt);
    return projPt;
  }

  public NonEncroachingSplitPointFinder() {
  }

  /**
   * A basic strategy for finding split points when nothing extra is known about the geometry of
   * the situation.
   *
   * @param seg the encroached segment
   * @param encroachPt the encroaching point
   * @return the point at which to split the encroached segment
   */
  @Override
  public Point findSplitPoint(final LineSegmentDoubleData seg, final Point encroachPt) {
    final LineSegment lineSeg = seg;
    final double segLen = lineSeg.getLength();
    final double midPtLen = segLen / 2;
    final SplitSegment splitSeg = new SplitSegment(lineSeg);

    final Point projPt = projectedSplitPoint(seg, encroachPt);
    /**
     * Compute the largest diameter (length) that will produce a split segment which is not
     * still encroached upon by the encroaching point (The length is reduced slightly by a
     * safety factor)
     */
    final double nonEncroachDiam = projPt.distancePoint(encroachPt) * 2 * 0.8; // .99;
    double maxSplitLen = nonEncroachDiam;
    if (maxSplitLen > midPtLen) {
      maxSplitLen = midPtLen;
    }
    splitSeg.setMinimumLength(maxSplitLen);

    splitSeg.splitAt(projPt);

    return splitSeg.getSplitPoint();
  }
}
