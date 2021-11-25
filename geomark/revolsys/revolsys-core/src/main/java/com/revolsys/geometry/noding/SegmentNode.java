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

import java.io.PrintStream;

import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Represents an intersection point between two {@link SegmentString}s.
 *
 * @version 1.7
 */
public class SegmentNode extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  private final boolean isInterior;

  private final int segmentIndex;

  private final int segmentOctant;

  public SegmentNode(final NodedSegmentString segString, final double x, final double y,
    final int segmentIndex, final int segmentOctant) {
    super(x, y);
    this.segmentIndex = segmentIndex;
    this.segmentOctant = segmentOctant;
    this.isInterior = !segString.equalsVertex2d(segmentIndex, x, y);
  }

  /**
   * @return -1 this SegmentNode is located before the argument location;
   * 0 this SegmentNode is at the argument location;
   * 1 this SegmentNode is located after the argument location
   */
  @Override
  public int compareTo(final Object object) {
    final SegmentNode other = (SegmentNode)object;
    if (this.segmentIndex < other.segmentIndex) {
      return -1;
    } else if (this.segmentIndex > other.segmentIndex) {
      return 1;
    } else {
      return SegmentPointComparator.compare(this.segmentOctant, this.x, this.y, other.x, other.y);
    }
  }

  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  public int getSegmentOctant() {
    return this.segmentOctant;
  }

  public boolean isEndPoint(final int maxSegmentIndex) {
    if (this.segmentIndex == 0 && !this.isInterior) {
      return true;
    }
    if (this.segmentIndex == maxSegmentIndex) {
      return true;
    }
    return false;
  }

  public boolean isInterior() {
    return this.isInterior;
  }

  public void print(final PrintStream out) {
    out.print(this);
    out.print(" seg # = " + this.segmentIndex);
  }
}
