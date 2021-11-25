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

import java.util.Collection;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class NodingValidator {

  private final LineIntersector li = new RobustLineIntersector();

  private final Collection<NodedSegmentString> segStrings;

  public NodingValidator(final Collection<NodedSegmentString> segStrings) {
    this.segStrings = segStrings;
  }

  private void checkCollapse(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    if (x1 == x3 && y1 == y3) {
      throw new RuntimeException("found non-noded collapse at "
        + GeometryFactory.DEFAULT_2D.lineString(2, x1, y1, x2, y2, x3, y3));
    }
  }

  /**
   * Checks if a segment string contains a segment pattern a-b-a (which implies a self-intersection)
   */
  private void checkCollapses() {
    for (final NodedSegmentString segment : this.segStrings) {
      checkCollapses(segment);
    }
  }

  private void checkCollapses(final NodedSegmentString ss) {
    final LineString points = ss.getLineString();
    for (int i = 0; i < points.getVertexCount() - 2; i++) {
      final double x1 = points.getX(i);
      final double y1 = points.getY(i);
      final double x2 = points.getX(i + 1);
      final double y2 = points.getY(i + 1);
      final double x3 = points.getX(i + 2);
      final double y3 = points.getY(i + 2);
      checkCollapse(x1, y1, x2, y2, x3, y3);
    }
  }

  /**
   * Checks for intersections between an endpoint of a segment string
   * and an interior vertex of another segment string
   */
  private void checkEndPtVertexIntersections() {
    for (final NodedSegmentString ss : this.segStrings) {
      final double x1 = ss.getX(0);
      final double y1 = ss.getX(0);
      checkEndPtVertexIntersections(x1, y1, this.segStrings);
      final int endIndex = ss.size() - 1;
      final double x2 = ss.getX(endIndex);
      final double y2 = ss.getY(endIndex);
      checkEndPtVertexIntersections(x2, y2, this.segStrings);
    }
  }

  private void checkEndPtVertexIntersections(final double x, final double y,
    final Collection<NodedSegmentString> segStrings) {
    for (final NodedSegmentString ss : segStrings) {
      final LineString pts = ss.getLineString();
      for (int j = 1; j < pts.getVertexCount() - 1; j++) {
        if (pts.equalsVertex2d(j, x, y)) {
          throw new RuntimeException(
            "found endpt/interior pt intersection at index " + j + " :pt " + x + "," + y);
        }
      }
    }
  }

  /**
   * Checks all pairs of segments for intersections at an interior point of a segment
   */
  private void checkInteriorIntersections() {
    for (final NodedSegmentString ss0 : this.segStrings) {
      for (final NodedSegmentString ss1 : this.segStrings) {
        checkInteriorIntersections(ss0, ss1);
      }
    }
  }

  private void checkInteriorIntersections(final NodedSegmentString e0, final int segIndex0,
    final SegmentString e1, final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    final LineString line1 = e0.getLineString();
    final double line1x1 = line1.getX(segIndex0);
    final double line1y1 = line1.getY(segIndex0);
    final double line1x2 = line1.getX(segIndex0 + 1);
    final double line1y2 = line1.getY(segIndex0 + 1);

    final LineString line2 = e1.getLineString();
    final double line2x1 = line2.getX(segIndex1);
    final double line2y1 = line2.getY(segIndex1);
    final double line2x2 = line2.getX(segIndex1 + 1);
    final double line2y2 = line2.getY(segIndex1 + 1);

    this.li.computeIntersectionLine(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2,
      line2y2);
    if (this.li.hasIntersection()) {

      if (this.li.isProper() || hasInteriorIntersection(this.li, line1x1, line1y1, line1x2, line1y2)
        || hasInteriorIntersection(this.li, line2x1, line2y1, line2x2, line2y2)) {
        throw new RuntimeException(
          "found non-noded intersection at " + line1x1 + "," + line1y1 + "-" + line1x2 + ","
            + line1y2 + " and " + line2x1 + "," + line2y1 + "-" + line2x2 + "," + line2y2);
      }
    }
  }

  private void checkInteriorIntersections(final NodedSegmentString ss0, final SegmentString ss1) {
    for (int i0 = 0; i0 < ss0.size() - 1; i0++) {
      for (int i1 = 0; i1 < ss1.size() - 1; i1++) {
        checkInteriorIntersections(ss0, i0, ss1, i1);
      }
    }
  }

  public void checkValid() {
    // MD - is this call required? Or could it be done in the Interior
    // Intersection code?
    checkEndPtVertexIntersections();
    checkInteriorIntersections();
    checkCollapses();
  }

  /**
   *@return true if there is an intersection point which is not an endpoint of the segment p0-p1
   */
  private boolean hasInteriorIntersection(final LineIntersector li, final double x1,
    final double y1, final double x2, final double y2) {
    for (int i = 0; i < li.getIntersectionCount(); i++) {
      final Point intPt = li.getIntersection(i);
      if (!(intPt.equalsVertex(x1, y1) || intPt.equalsVertex(x2, y2))) {
        return true;
      }
    }
    return false;
  }

}
