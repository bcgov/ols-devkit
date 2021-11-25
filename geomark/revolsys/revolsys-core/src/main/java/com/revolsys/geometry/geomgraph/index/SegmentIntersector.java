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
package com.revolsys.geometry.geomgraph.index;

import java.util.Collection;
import java.util.Collections;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

/**
 * Computes the intersection of line segments,
 * and adds the intersection to the edges containing the segments.
 *
 * @version 1.7
 */
public class SegmentIntersector {
  /**
   * These variables keep track of what types of intersections were
   * found during ALL edges that have been intersected.
   */
  private boolean hasIntersection = false;

  private boolean hasProper = false;

  private boolean hasProperInterior = false;

  private final boolean includeProper;

  private final LineIntersector lineIntersector;

  // testing only
  public int numTests = 0;

  // the proper intersection point found
  private Point properIntersectionPoint = null;

  private final boolean recordIsolated;

  private Collection<Node> boundaryNodes1 = Collections.emptySet();

  private Collection<Node> boundaryNodes2 = Collections.emptySet();

  /*
   * public SegmentIntersector() { }
   */
  public SegmentIntersector(final LineIntersector li, final boolean includeProper,
    final boolean recordIsolated) {
    this.lineIntersector = li;
    this.includeProper = includeProper;
    this.recordIsolated = recordIsolated;
  }

  public void addIntersections(final Edge edge1, final LineString line1, final int fromIndex1,
    final double fromX1, final double fromY1, final double toX1, final double toY1,
    final Edge edge2, final int fromIndex2, final double fromX2, final double fromY2,
    final double toX2, final double toY2) {
    if (edge1 == edge2 && fromIndex1 == fromIndex2) {
      return;
    } else {
      this.numTests++;

      final LineIntersector lineIntersector = this.lineIntersector;
      lineIntersector.computeIntersectionLine(fromX1, fromY1, toX1, toY1, fromX2, fromY2, toX2,
        toY2);
      /**
       *  Always record any non-proper intersections.
       *  If includeProper is true, record any proper intersections as well.
       */
      if (lineIntersector.hasIntersection()) {
        if (this.recordIsolated) {
          edge1.setIsolated(false);
          edge2.setIsolated(false);
        }
        // if the segments are adjacent they have at least one trivial
        // intersection,
        // the shared endpoint. Don't bother adding it if it is the
        // only intersection.
        boolean trivialIntersection = false;
        if (edge1 == edge2) {
          if (lineIntersector.getIntersectionCount() == 1) {
            if (Math.abs(fromIndex1 - fromIndex2) == 1) {
              trivialIntersection = true;
            } else if (line1.isClosed()) {
              final int maxSegIndex = line1.getVertexCount() - 1;
              if (fromIndex1 == 0 && fromIndex2 == maxSegIndex
                || fromIndex2 == 0 && fromIndex1 == maxSegIndex) {
                trivialIntersection = true;
              }
            }
          }
        }

        if (!trivialIntersection) {
          this.hasIntersection = true;
          if (this.includeProper || !lineIntersector.isProper()) {
            edge1.addIntersections(lineIntersector, fromIndex1, 0);
            edge2.addIntersections(lineIntersector, fromIndex2, 1);
          }
          if (lineIntersector.isProper()) {
            this.properIntersectionPoint = lineIntersector.getIntersection(0);
            this.hasProper = true;
            if (!isBoundaryPoint()) {
              this.hasProperInterior = true;
            }
          }
        }
      }
    }
  }

  /**
   * This method is called by clients of the EdgeIntersector class to test for and add
   * intersections for two segments of the edges being intersected.
   * Note that clients (such as MonotoneChainEdges) may choose not to intersect
   * certain pairs of segments for efficiency reasons.
   */
  public void addIntersections(final Edge edge1, final LineString line1, final int segIndex0,
    final Edge edge2, final LineString line2, final int segIndex1) {
    if (edge1 == edge2 && segIndex0 == segIndex1) {
      return;
    } else {
      this.numTests++;
      final double line1x1 = line1.getX(segIndex0);
      final double line1y1 = line1.getY(segIndex0);
      final double line1x2 = line1.getX(segIndex0 + 1);
      final double line1y2 = line1.getY(segIndex0 + 1);

      final double line2x1 = line2.getX(segIndex1);
      final double line2y1 = line2.getY(segIndex1);
      final double line2x2 = line2.getX(segIndex1 + 1);
      final double line2y2 = line2.getY(segIndex1 + 1);

      final LineIntersector lineIntersector = this.lineIntersector;
      lineIntersector.computeIntersectionLine(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
        line2x2, line2y2);
      /**
       *  Always record any non-proper intersections.
       *  If includeProper is true, record any proper intersections as well.
       */
      if (lineIntersector.hasIntersection()) {
        if (this.recordIsolated) {
          edge1.setIsolated(false);
          edge2.setIsolated(false);
        }
        // if the segments are adjacent they have at least one trivial
        // intersection,
        // the shared endpoint. Don't bother adding it if it is the
        // only intersection.
        boolean trivialIntersection = false;
        if (edge1 == edge2) {
          if (lineIntersector.getIntersectionCount() == 1) {
            if (Math.abs(segIndex0 - segIndex1) == 1) {
              trivialIntersection = true;
            } else if (line1.isClosed()) {
              final int maxSegIndex = line1.getVertexCount() - 1;
              if (segIndex0 == 0 && segIndex1 == maxSegIndex
                || segIndex1 == 0 && segIndex0 == maxSegIndex) {
                trivialIntersection = true;
              }
            }
          }
        }

        if (!trivialIntersection) {
          this.hasIntersection = true;
          if (this.includeProper || !lineIntersector.isProper()) {
            edge1.addIntersections(lineIntersector, segIndex0, 0);
            edge2.addIntersections(lineIntersector, segIndex1, 1);
          }
          if (lineIntersector.isProper()) {
            this.properIntersectionPoint = lineIntersector.getIntersection(0);
            this.hasProper = true;
            if (!isBoundaryPoint()) {
              this.hasProperInterior = true;
            }
          }
        }
      }
    }
  }

  /**
   * @return the proper intersection point, or <code>null</code> if none was found
   */
  public Point getProperIntersectionPoint() {
    return this.properIntersectionPoint;
  }

  public boolean hasIntersection() {
    return this.hasIntersection;
  }

  /**
   * A proper interior intersection is a proper intersection which is <b>not</b>
   * contained in the set of boundary nodes set for this SegmentIntersector.
   */
  public boolean hasProperInteriorIntersection() {
    return this.hasProperInterior;
  }

  /**
   * A proper intersection is an intersection which is interior to at least two
   * line segments.  Note that a proper intersection is not necessarily
   * in the interior of the entire Geometry, since another edge may have
   * an endpoint equal to the intersection, which according to SFS semantics
   * can result in the point being on the Boundary of the Geometry.
   */
  public boolean hasProperIntersection() {
    return this.hasProper;
  }

  private boolean isBoundaryPoint() {
    if (isBoundaryPoint(this.boundaryNodes1)) {
      return true;
    } else if (isBoundaryPoint(this.boundaryNodes2)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isBoundaryPoint(final Collection<Node> boundaryNodes) {
    final LineIntersector lineIntersector = this.lineIntersector;
    for (final Node node : boundaryNodes) {
      final double x = node.getX();
      final double y = node.getY();
      if (lineIntersector.isIntersection(x, y)) {
        return true;
      }
    }
    return false;
  }

  public void setBoundaryNodes(final Collection<Node> boundaryNodes1,
    final Collection<Node> boundaryNodes2) {
    this.boundaryNodes1 = boundaryNodes1;
    this.boundaryNodes2 = boundaryNodes2;
  }

}
