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
package com.revolsys.geometry.operation.buffer;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.LineStringDouble;

/**
 * Simplifies a buffer input line to
 * remove concavities with shallow depth.
 * <p>
 * The most important benefit of doing this
 * is to reduce the number of points and the complexity of
 * shape which will be buffered.
 * It also reduces the risk of gores created by
 * the quantized fillet arcs (although this issue
 * should be eliminated in any case by the
 * offset curve generation logic).
 * <p>
 * A key aspect of the simplification is that it
 * affects inside (concave or inward) corners only.
 * Convex (outward) corners are preserved, since they
 * are required to ensure that the generated buffer curve
 * lies at the correct distance from the input geometry.
 * <p>
 * Another important heuristic used is that the end segments
 * of the input are never simplified.  This ensures that
 * the client buffer code is able to generate end caps faithfully.
 * <p>
 * No attempt is made to avoid self-intersections in the output.
 * This is acceptable for use for generating a buffer offset curve,
 * since the buffer algorithm is insensitive to invalid polygonal
 * geometry.  However,
 * this means that this algorithm
 * cannot be used as a general-purpose polygon simplification technique.
 *
 * @author Martin Davis
 *
 */
public class BufferInputLineSimplifier {
  private static final int DELETE = 1;

  private static final int NUM_PTS_TO_CHECK = 10;

  /**
   * Simplify the input coordinate list.
   * If the distance tolerance is positive,
   * concavities on the LEFT side of the line are simplified.
   * If the supplied distance tolerance is negative,
   * concavities on the RIGHT side of the line are simplified.
   *
   * @param inputLine the coordinate list to simplify
   * @param distanceTol simplification distance tolerance to use
   * @return the simplified coordinate list
   */
  public static LineString simplify(final LineString inputLine, final double distanceTol) {
    final BufferInputLineSimplifier simp = new BufferInputLineSimplifier(inputLine);
    return simp.simplify(distanceTol);
  }

  private int angleOrientation = CGAlgorithms.COUNTERCLOCKWISE;

  private int deleteCount = 0;

  private double distanceTol;

  private final LineString inputLine;

  private byte[] isDeleted;

  public BufferInputLineSimplifier(final LineString inputLine) {
    this.inputLine = inputLine;
  }

  private LineString collapseLine() {
    final int axisCount = this.inputLine.getAxisCount();
    final int vertexCount = this.inputLine.getVertexCount();
    final double[] coordinates = new double[(vertexCount - this.deleteCount) * axisCount];
    int j = 0;
    for (int i = 0; i < vertexCount; i++) {
      if (this.isDeleted[i] != DELETE) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, j++, this.inputLine, i);
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  /**
   * Uses a sliding window containing 3 vertices to detect shallow angles
   * in which the middle vertex can be deleted, since it does not
   * affect the shape of the resulting buffer in a significant way.
   * @return
   */
  private boolean deleteShallowConcavities() {
    /**
     * Do not simplify end line segments of the line string.
     * This ensures that end caps are generated consistently.
     */
    int index = 1;

    int midIndex = findNextNonDeletedIndex(index);
    int lastIndex = findNextNonDeletedIndex(midIndex);

    boolean isChanged = false;
    while (lastIndex < this.inputLine.getVertexCount()) {
      // test triple for shallow concavity
      boolean isMiddleVertexDeleted = false;
      if (isDeletable(index, midIndex, lastIndex, this.distanceTol)) {
        this.deleteCount++;
        this.isDeleted[midIndex] = DELETE;
        isMiddleVertexDeleted = true;
        isChanged = true;
      }
      // move simplification window forward
      if (isMiddleVertexDeleted) {
        index = lastIndex;
      } else {
        index = midIndex;
      }

      midIndex = findNextNonDeletedIndex(index);
      lastIndex = findNextNonDeletedIndex(midIndex);
    }
    return isChanged;
  }

  /**
   * Finds the next non-deleted index, or the end of the point array if none
   * @param index
   * @return the next non-deleted index, if any
   * or inputLine.length if there are no more non-deleted indices
   */
  private int findNextNonDeletedIndex(final int index) {
    int next = index + 1;
    while (next < this.inputLine.getVertexCount() && this.isDeleted[next] == DELETE) {
      next++;
    }
    return next;
  }

  private boolean isConcave(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final int orientation = CGAlgorithmsDD.orientationIndex(x1, y1, x2, y2, x3, y3);
    final boolean isConcave = orientation == this.angleOrientation;
    return isConcave;
  }

  private boolean isDeletable(final int i0, final int i1, final int i2, final double distanceTol) {
    final double x1 = this.inputLine.getX(i0);
    final double y1 = this.inputLine.getY(i0);
    final double x2 = this.inputLine.getX(i1);
    final double y2 = this.inputLine.getY(i1);
    final double x3 = this.inputLine.getX(i2);
    final double y3 = this.inputLine.getY(i2);

    if (!isConcave(x1, y1, x2, y2, x3, y3)) {
      return false;
    }
    if (!isShallow(x1, y1, x2, y2, x3, y3, distanceTol)) {
      return false;
    }

    // MD - don't use this heuristic - it's too restricting
    // if (p0.distance(p2) > distanceTol) return false;

    return isShallowSampled(x1, y1, x2, y2, i0, i2, distanceTol);
  }

  private boolean isShallow(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3, final double distanceTol) {
    final double dist = LineSegmentUtil.distanceLinePoint(x1, y1, x3, y3, x2, y2);
    return dist < distanceTol;
  }

  /**
   * Checks for shallowness over a sample of points in the given section.
   * This helps prevents the siplification from incrementally
   * "skipping" over points which are in fact non-shallow.
   *
   * @param p0 start coordinate of section
   * @param p2 end coordinate of section
   * @param i0 start index of section
   * @param i2 end index of section
   * @param distanceTol distance tolerance
   * @return
   */
  private boolean isShallowSampled(final double x1, final double y1, final double x2,
    final double y2, final int i0, final int i2, final double distanceTol) {
    // check every n'th point to see if it is within tolerance
    int inc = (i2 - i0) / NUM_PTS_TO_CHECK;
    if (inc <= 0) {
      inc = 1;
    }

    for (int i = i0; i < i2; i += inc) {
      final double x3 = this.inputLine.getX(i);
      final double y3 = this.inputLine.getY(i);
      if (!isShallow(x1, y1, x2, y2, x3, y3, distanceTol)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Simplify the input coordinate list.
   * If the distance tolerance is positive,
   * concavities on the LEFT side of the line are simplified.
   * If the supplied distance tolerance is negative,
   * concavities on the RIGHT side of the line are simplified.
   *
   * @param distanceTol simplification distance tolerance to use
   * @return the simplified coordinate list
   */
  public LineString simplify(final double distanceTol) {
    this.distanceTol = Math.abs(distanceTol);
    if (distanceTol < 0) {
      this.angleOrientation = CGAlgorithms.CLOCKWISE;
    }

    // rely on fact that boolean array is filled with false value
    this.isDeleted = new byte[this.inputLine.getVertexCount()];

    boolean isChanged = false;
    do {
      isChanged = deleteShallowConcavities();
    } while (isChanged);

    return collapseLine();
  }
}
