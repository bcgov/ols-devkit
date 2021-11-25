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

package com.revolsys.geometry.operation.polygonize;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.PointList;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.planargraph.DirectedEdge;

/**
 * Represents a ring of {@link PolygonizeDirectedEdge}s which form
 * a ring of a polygon.  The ring may be either an outer shell or a hole.
 *
 * @version 1.7
 */
class EdgeRing implements BoundingBoxProxy {

  private static void addEdge(final LineString coords, final boolean isForward,
    final PointList coordList) {
    if (isForward) {
      for (int i = 0; i < coords.getVertexCount(); i++) {
        coordList.add(coords.getPoint(i), false);
      }
    } else {
      for (int i = coords.getVertexCount() - 1; i >= 0; i--) {
        coordList.add(coords.getPoint(i), false);
      }
    }
  }

  /**
   * Find the innermost enclosing shell EdgeRing containing the argument EdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B iff envelope(ring A) contains envelope(ring B)
   * <br>
   * This routine is only safe to use if the chosen point of the hole
   * is known to be properly contained in a shell
   * (which is guaranteed to be the case if the hole does not touch its shell)
   *
   * @return containing EdgeRing, if there is one
   * or null if no containing EdgeRing is found
   */
  public static EdgeRing findEdgeRingContaining(final EdgeRing testEr,
    final List<EdgeRing> shellList) {
    final LinearRing testRing = testEr.getRing();
    Point testPt = testRing.getPoint(0);
    if (testPt == null) {
      return null;
    } else {
      EdgeRing minShell = null;
      for (final EdgeRing tryShell : shellList) {
        final LinearRing tryShellRing = tryShell.getRing();
        // the hole envelope cannot equal the shell envelope
        // (also guards against testing rings against themselves)
        if (tryShell.bboxEquals(testRing)) {
          continue;
        }
        // hole must be contained in shell
        if (!tryShell.bboxCovers(testRing)) {
          continue;
        }

        testPt = CoordinatesUtil.pointNotInList(testRing, tryShellRing);
        boolean isContained = false;
        if (tryShellRing.isPointInRing(testPt)) {
          isContained = true;
        }

        // check if this new containing ring is smaller than the current minimum
        // ring
        if (isContained) {
          if (minShell == null || minShell.bboxCovers(tryShellRing)) {
            minShell = tryShell;
          }
        }
      }
      return minShell;
    }
  }

  /**
   * Tests whether a given point is in an array of points.
   * Uses a value-based test.
   *
   * @param pt a {@link Coordinates} for the test point
   * @param pts an array of {@link Coordinates}s to test
   * @return <code>true</code> if the point is in the array
   *
   * @deprecated
   */
  @Deprecated
  public static boolean isInList(final Point pt, final Point[] pts) {
    for (final Point pt2 : pts) {
      if (pt.equals(pt2)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Finds a point in a list of points which is not contained in another list of points
   * @param testPts the {@link Coordinates}s to test
   * @param pts an array of {@link Coordinates}s to test the input points against
   * @return a {@link Coordinates} from <code>testPts</code> which is not in <code>pts</code>,
   * or null if there is no coordinate not in the list
   *
   * @deprecated Use CoordinateArrays.ptNotInList instead
   */
  @Deprecated
  public static Point ptNotInList(final Point[] testPts, final Point[] pts) {
    for (final Point testPt2 : testPts) {
      final Point testPt = testPt2;
      if (!isInList(testPt, pts)) {
        return testPt;
      }
    }
    return null;
  }

  private final List<DirectedEdge> deList = new ArrayList<>();

  private final GeometryFactory factory;

  private List<LinearRing> holes;

  // cache the following data for efficiency
  private LinearRing ring = null;

  private Point[] ringPts = null;

  public EdgeRing(final GeometryFactory factory) {
    this.factory = factory;
  }

  /**
   * Adds a {@link DirectedEdge} which is known to form part of this ring.
   * @param de the {@link DirectedEdge} to add.
   */
  public void add(final DirectedEdge de) {
    this.deList.add(de);
  }

  /**
   * Adds a hole to the polygon formed by this ring.
   * @param hole the {@link LinearRing} forming the hole.
   */
  public void addHole(final LinearRing hole) {
    if (this.holes == null) {
      this.holes = new ArrayList<>();
    }
    this.holes.add(hole);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.ring.getBoundingBox();
  }

  /**
   * Computes the list of coordinates which are contained in this ring.
   * The coordinatea are computed once only and cached.
   *
   * @return an array of the {@link Coordinates}s in this ring
   */
  private Point[] getCoordinates() {
    if (this.ringPts == null) {
      final PointList coordList = new PointList();
      for (final DirectedEdge de : this.deList) {
        final PolygonizeEdge edge = (PolygonizeEdge)de.getEdge();
        addEdge(edge.getLine(), de.getEdgeDirection(), coordList);
      }
      this.ringPts = coordList.toPointArray();
    }
    return this.ringPts;
  }

  /**
   * Gets the coordinates for this ring as a {@link LineString}.
   * Used to return the coordinates in this ring
   * as a valid geometry, when it has been detected that the ring is topologically
   * invalid.
   * @return a {@link LineString} containing the coordinates in this ring
   */
  public LineString getLineString() {
    getCoordinates();
    return this.factory.lineString(this.ringPts);
  }

  /**
   * Computes the {@link Polygon} formed by this ring and any contained holes.
   *
   * @return the {@link Polygon} formed by this ring and its holes.
   */
  public Polygon getPolygon() {
    final List<LinearRing> rings = new ArrayList<>();
    rings.add(this.ring);
    if (this.holes != null) {
      rings.addAll(this.holes);
    }
    final Polygon poly = this.factory.polygon(rings);
    return poly;
  }

  /**
   * Returns this ring as a {@link LinearRing}, or null if an Exception occurs while
   * creating it (such as a topology problem). Details of problems are written to
   * standard output.
   */
  public LinearRing getRing() {
    if (this.ring != null) {
      return this.ring;
    }
    getCoordinates();
    if (this.ringPts.length < 3) {
      System.out.println(this.ringPts);
    }
    try {
      this.ring = this.factory.linearRing(this.ringPts);
    } catch (final Exception ex) {
      System.out.println(this.ringPts);
    }
    return this.ring;
  }

  /**
   * Tests whether this ring is a hole.
   * Due to the way the edges in the polyongization graph are linked,
   * a ring is a hole if it is oriented counter-clockwise.
   * @return <code>true</code> if this ring is a hole
   */
  public boolean isHole() {
    final LinearRing ring = getRing();
    return ring.isCounterClockwise();
  }

  /**
   * Tests if the {@link LinearRing} ring formed by this edge ring is topologically valid.
   *
   * @return true if the ring is valid
   */
  public boolean isValid() {
    getCoordinates();
    if (this.ringPts.length <= 3) {
      return false;
    }
    getRing();
    return this.ring.isValid();
  }

  @Override
  public String toString() {
    if (this.ring == null) {
      return this.ringPts.toString();
    } else {
      return this.ring.toString();
    }
  }
}
