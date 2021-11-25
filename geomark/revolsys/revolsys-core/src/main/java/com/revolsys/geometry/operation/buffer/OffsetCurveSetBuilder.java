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

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.util.TriangleImpl;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.SegmentString;

/**
 * Creates all the raw offset curves for a buffer of a {@link Geometry}.
 * Raw curves need to be noded together and polygonized to form the final buffer area.
 *
 * @version 1.7
 */
public class OffsetCurveSetBuilder {

  private final OffsetCurveBuilder curveBuilder;

  private final List<NodedSegmentString> curveList = new ArrayList<>();

  private final double distance;

  private final Geometry geometry;

  public OffsetCurveSetBuilder(final Geometry inputGeom, final double distance,
    final GeometryFactory precisionModel, final BufferParameters parameters) {
    this.geometry = inputGeom;
    this.distance = distance;
    this.curveBuilder = new OffsetCurveBuilder(precisionModel, parameters);
  }

  public OffsetCurveSetBuilder(final Geometry inputGeom, final double distance,
    final OffsetCurveBuilder curveBuilder) {
    this.geometry = inputGeom;
    this.distance = distance;
    this.curveBuilder = curveBuilder;
  }

  private void add(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return;
    } else if (geometry instanceof Polygon) {
      addPolygon((Polygon)geometry);
    } else if (geometry instanceof LineString) {
      addLineString((LineString)geometry);
    } else if (geometry instanceof Point) {
      addPoint((Point)geometry);
    } else {
      for (final Geometry part : geometry.geometries()) {
        add(part);
      }
    }
  }

  /**
   * Creates a {@link SegmentString} for a coordinate list which is a raw offset curve,
   * and adds it to the list of buffer curves.
   * The SegmentString is tagged with a Label giving the topology of the curve.
   * The curve may be oriented in either direction.
   * If the curve is oriented CW, the locations will be:
   * <br>Left: Location.EXTERIOR
   * <br>Right: Location.INTERIOR
   */
  private void addCurve(final LineString points, final Location leftLoc, final Location rightLoc) {
    if (points != null && points.getVertexCount() >= 2) {
      final Label label = new Label(0, Location.BOUNDARY, leftLoc, rightLoc);
      final NodedSegmentString segment = new NodedSegmentString(points, label);
      this.curveList.add(segment);
    }
  }

  private void addLineString(final LineString line) {
    // a zero or negative width buffer of a line/point is empty
    if (this.distance <= 0.0 && !this.curveBuilder.getBufferParameters().isSingleSided()) {
      return;
    } else {
      final LineString points = line.removeDuplicatePoints();
      final LineString curve = this.curveBuilder.getLineCurve(points, this.distance);
      addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
    }
  }

  /**
   * Add a Point to the graph.
   */
  private void addPoint(final Point point) {
    // a zero or negative width buffer of a line/point is empty
    if (this.distance > 0.0) {
      final LineString curve = this.curveBuilder.getPointCurve(point, this.distance);
      addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
    }
  }

  private void addPolygon(final Polygon p) {
    double offsetDistance = this.distance;
    int offsetSide = Position.LEFT;
    if (this.distance < 0.0) {
      offsetDistance = -this.distance;
      offsetSide = Position.RIGHT;
    }

    final LinearRing shell = p.getShell();
    final boolean shellClockwise = shell.isClockwise();
    final LinearRing shellCoord = shell.removeDuplicatePoints();
    if (this.distance < 0.0 && isErodedCompletely(shell, this.distance)) {
      // optimization - don't bother computing buffer
      // if the polygon would be completely eroded
    } else if (this.distance <= 0.0 && shellCoord.getVertexCount() < 3) {
      // don't attempt to buffer a polygon with too few distinct vertices
    } else {

      addPolygonRing(shellCoord, shellClockwise, offsetDistance, offsetSide, Location.EXTERIOR,
        Location.INTERIOR);

      for (int i = 0; i < p.getHoleCount(); i++) {
        final LinearRing hole = p.getHole(i);
        final boolean holeClockwise = hole.isClockwise();
        final LinearRing holeCoord = hole.removeDuplicatePoints();

        // optimization - don't bother computing buffer for this hole
        // if the hole would be completely covered
        if (!(this.distance > 0.0 && isErodedCompletely(hole, -this.distance))) {
          // Holes are topologically labeled opposite to the shell, since
          // the interior of the polygon lies on their opposite side
          // (on the left, if the hole is oriented CCW)
          final int opposite = Position.opposite(offsetSide);
          addPolygonRing(holeCoord, holeClockwise, offsetDistance, opposite, Location.INTERIOR,
            Location.EXTERIOR);
        }
      }
    }
  }

  /**
   * Adds an offset curve for a polygon ring.
   * The side and left and right topological location arguments
   * assume that the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations must be interchanged and the side flipped.
   *
   * @param points the coordinates of the ring (must not contain repeated points)
   * @param offsetDistance the distance at which to create the buffer
   * @param side the side of the ring on which to construct the buffer line
   * @param cwLeftLoc the location on the L side of the ring (if it is CW)
   * @param cwRightLoc the location on the R side of the ring (if it is CW)
   */
  private void addPolygonRing(final LineString points, final boolean clockwise,
    final double offsetDistance, int side, final Location cwLeftLoc, final Location cwRightLoc) {
    // don't bother adding ring if it is "flat" and will disappear in the output
    if (offsetDistance == 0.0 && points.getVertexCount() < 4) {
      return;
    }

    Location leftLoc = cwLeftLoc;
    Location rightLoc = cwRightLoc;
    if (points.getVertexCount() >= 4 && !clockwise) {
      leftLoc = cwRightLoc;
      rightLoc = cwLeftLoc;
      side = Position.opposite(side);
    }
    final LineString curve = this.curveBuilder.getRingCurve(points, side, offsetDistance);
    addCurve(curve, leftLoc, rightLoc);
  }

  /**
   * Computes the set of raw offset curves for the buffer.
   * Each offset curve has an attached {@link Label} indicating
   * its left and right location.
   *
   * @return a Collection of SegmentStrings representing the raw buffer curves
   */
  public List<NodedSegmentString> getCurves() {
    add(this.geometry);
    return this.curveList;
  }

  /**
   * The ringCoord is assumed to contain no repeated points.
   * It may be degenerate (i.e. contain only 1, 2, or 3 points).
   * In this case it has no area, and hence has a minimum diameter of 0.
   *
   * @param ringCoord
   * @param offsetDistance
   * @return
   */
  private boolean isErodedCompletely(final LinearRing ring, final double bufferDistance) {
    // degenerate ring has no area
    if (ring.getVertexCount() < 4) {
      return bufferDistance < 0;
    }

    // important test to eliminate inverted triangle bug
    // also optimizes erosion test for triangles
    if (ring.getVertexCount() == 4) {
      return isTriangleErodedCompletely(ring, bufferDistance);
    }

    // if envelope is narrower than twice the buffer distance, ring is eroded
    final BoundingBox env = ring.getBoundingBox();
    final double envMinDimension = Math.min(env.getHeight(), env.getWidth());
    if (bufferDistance < 0.0 && 2 * Math.abs(bufferDistance) > envMinDimension) {
      return true;
    }

    return false;
  }

  /**
   * Tests whether a triangular ring would be eroded completely by the given
   * buffer distance.
   * This is a precise test.  It uses the fact that the inner buffer of a
   * triangle converges on the inCentre of the triangle (the point
   * equidistant from all sides).  If the buffer distance is greater than the
   * distance of the inCentre from a side, the triangle will be eroded completely.
   *
   * This test is important, since it removes a problematic case where
   * the buffer distance is slightly larger than the inCentre distance.
   * In this case the triangle buffer curve "inverts" with incorrect topology,
   * producing an incorrect hole in the buffer.
   *
   * @param triangleCoord
   * @param bufferDistance
   * @return
   */
  private boolean isTriangleErodedCompletely(final LinearRing triangleCoord,
    final double bufferDistance) {
    final TriangleImpl tri = new TriangleImpl(triangleCoord.getVertex(0),
      triangleCoord.getVertex(1), triangleCoord.getVertex(2));
    final Point inCentre = tri.inCentre();
    final double distToCentre = LineSegmentUtil.distanceLinePoint(tri.p0, tri.p1, inCentre);
    return distToCentre < Math.abs(bufferDistance);
  }

}
