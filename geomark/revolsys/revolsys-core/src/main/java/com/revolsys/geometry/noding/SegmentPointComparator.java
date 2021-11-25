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

package com.revolsys.geometry.noding;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Assert;

/**
 * Implements a robust method of comparing the relative position of two
 * points along the same segment.
 * The coordinates are assumed to lie "near" the segment.
 * This means that this algorithm will only return correct results
 * if the input coordinates
 * have the same precision and correspond to rounded values
 * of exact coordinates lying on the segment.
 *
 * @version 1.7
 */
public class SegmentPointComparator {

  public static int compare(final int octant, final double x1, final double y1, final double x2,
    final double y2) {
    // nodes can only be equal if their coordinates are equal
    if (x1 == x2 && y1 == y2) {
      return 0;
    } else {

      final int xSign = relativeSign(x1, x2);
      final int ySign = relativeSign(y1, y2);

      switch (octant) {
        case 0:
          return compareValue(xSign, ySign);
        case 1:
          return compareValue(ySign, xSign);
        case 2:
          return compareValue(ySign, -xSign);
        case 3:
          return compareValue(-xSign, ySign);
        case 4:
          return compareValue(-xSign, -ySign);
        case 5:
          return compareValue(-ySign, -xSign);
        case 6:
          return compareValue(-ySign, xSign);
        case 7:
          return compareValue(xSign, -ySign);
        default:
          Assert.shouldNeverReachHere("invalid octant value");
          return 0;
      }
    }
  }

  /**
   * Compares two {@link Coordinates}s for their relative position along a segment
   * lying in the specified {@link Octant}.
   *
   * @return -1 node0 occurs first;
   * 0 the two nodes are equal;
   * 1 node1 occurs first
   */
  public static int compare(final int octant, final Point p0, final Point p1) {
    final double x1 = p0.getX();
    final double y1 = p0.getY();
    final double x2 = p1.getX();
    final double y2 = p1.getY();
    return compare(octant, x1, y1, x2, y2);
  }

  private static int compareValue(final int compareSign0, final int compareSign1) {
    if (compareSign0 < 0) {
      return -1;
    }
    if (compareSign0 > 0) {
      return 1;
    }
    if (compareSign1 < 0) {
      return -1;
    }
    if (compareSign1 > 0) {
      return 1;
    }
    return 0;

  }

  public static int relativeSign(final double x0, final double x1) {
    if (x0 < x1) {
      return -1;
    }
    if (x0 > x1) {
      return 1;
    }
    return 0;
  }
}
