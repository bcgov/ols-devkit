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
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.LineSegment;

/**
 * Models a constraint segment which can be split in two in various ways,
 * according to certain geometric constraints.
 *
 * @author Martin Davis
 */
public class SplitSegment {
  /**
   * Computes the {@link Coordinates} that lies a given fraction along the line defined by the
   * reverse of the given segment. A fraction of <code>0.0</code> returns the end point of the
   * segment; a fraction of <code>1.0</code> returns the start point of the segment.
   *
   * @param seg the LineSegmentDouble
   * @param segmentLengthFraction the fraction of the segment length along the line
   * @return the point at that distance
   */
  private static Point pointAlongReverse(final LineSegment seg,
    final double segmentLengthFraction) {
    final double x = seg.getP1().getX()
      - segmentLengthFraction * (seg.getP1().getX() - seg.getP0().getX());
    final double y = seg.getP1().getY()
      - segmentLengthFraction * (seg.getP1().getY() - seg.getP0().getY());
    final Point coord = new PointDoubleXY(x, y);
    return coord;
  }

  private double minimumLen = 0.0;

  private final LineSegment seg;

  private final double segLen;

  private Point splitPt;

  public SplitSegment(final LineSegment seg) {
    this.seg = seg;
    this.segLen = seg.getLength();
  }

  private double getConstrainedLength(final double len) {
    if (len < this.minimumLen) {
      return this.minimumLen;
    }
    return len;
  }

  public Point getSplitPoint() {
    return this.splitPt;
  }

  public void setMinimumLength(final double minLen) {
    this.minimumLen = minLen;
  }

  public void splitAt(final double length, final Point endPt) {
    final double actualLen = getConstrainedLength(length);
    final double frac = actualLen / this.segLen;
    if (endPt.equals(2, this.seg.getP0())) {
      this.splitPt = this.seg.pointAlong(frac);
    } else {
      this.splitPt = pointAlongReverse(this.seg, frac);
    }
  }

  public void splitAt(final Point pt) {
    // check that given pt doesn't violate min length
    final double minFrac = this.minimumLen / this.segLen;
    if (pt.distancePoint(this.seg.getP0()) < this.minimumLen) {
      this.splitPt = this.seg.pointAlong(minFrac);
      return;
    }
    if (pt.distancePoint(this.seg.getP1()) < this.minimumLen) {
      this.splitPt = pointAlongReverse(this.seg, minFrac);
      return;
    }
    // passes minimum distance check - use provided point as split pt
    this.splitPt = pt;
  }

}
