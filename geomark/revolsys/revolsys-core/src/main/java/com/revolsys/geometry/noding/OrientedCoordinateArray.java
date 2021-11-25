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

import com.revolsys.geometry.model.LineString;

/**
 * Allows comparing {@link Coordinates} arrays
 * in an orientation-independent way.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class OrientedCoordinateArray implements Comparable<OrientedCoordinateArray> {
  /**
   * Determines which orientation of the {@link Coordinates} array
   * is (overall) increasing.
   * In other words, determines which end of the array is "smaller"
   * (using the standard ordering on {@link Coordinates}).
   * Returns an integer indicating the increasing direction.
   * If the sequence is a palindrome, it is defined to be
   * oriented in a positive direction.
   *
   * @param line the array of Point to test
   * @return <code>1</code> if the array is smaller at the start
   * or is a palindrome,
   * <code>-1</code> if smaller at the end
   */
  public static int increasingDirection(final LineString line) {
    final int numPoints = line.getVertexCount();
    for (int vertextIndex = 0; vertextIndex < numPoints / 2; vertextIndex++) {
      final int j = numPoints - 1 - vertextIndex;
      // skip equal points on both ends
      final int comp = line.compareVertex(vertextIndex, j);
      if (comp != 0) {
        return comp;
      }
    }
    // array must be a palindrome - defined to be in positive direction
    return 1;
  }

  private final boolean orientation;

  private final LineString points;

  /**
   * Creates a new {@link OrientedCoordinateArray}
   * for the given {@link Coordinates} array.
   *
   * @param points the coordinates to orient
   */
  public OrientedCoordinateArray(final LineString points) {
    this.points = points;
    this.orientation = increasingDirection(points) == 1;
  }

  /**
   * Compares two {@link OrientedCoordinateArray}s for their relative order
   *
   * @return -1 this one is smaller;
   * 0 the two objects are equal;
   * 1 this one is greater
   */

  @Override
  public int compareTo(final OrientedCoordinateArray oca) {
    final LineString points1 = getPoints();
    final LineString points2 = oca.getPoints();
    final boolean orientation2 = oca.getOrientation();
    final int dir1 = this.orientation ? 1 : -1;
    final int dir2 = orientation2 ? 1 : -1;
    final int vertexCount1 = points1.getVertexCount();
    final int vertexCount2 = points2.getVertexCount();
    final int limit1 = this.orientation ? vertexCount1 : -1;
    final int limit2 = orientation2 ? vertexCount2 : -1;

    int i1 = this.orientation ? 0 : vertexCount1 - 1;
    int i2 = orientation2 ? 0 : vertexCount2 - 1;
    while (true) {
      final int compPt = points1.compareVertex(i1, points2, i2);
      if (compPt != 0) {
        return compPt;
      } else {
        i1 += dir1;
        i2 += dir2;
        final boolean done1 = i1 == limit1;
        final boolean done2 = i2 == limit2;
        if (done1 && !done2) {
          return -1;
        } else if (!done1 && done2) {
          return 1;
        } else if (done1 && done2) {
          return 0;
        }
      }
    }
  }

  public boolean getOrientation() {
    return this.orientation;
  }

  public LineString getPoints() {
    return this.points;
  }
}
