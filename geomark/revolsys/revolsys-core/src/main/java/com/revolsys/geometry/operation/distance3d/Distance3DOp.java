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
package com.revolsys.geometry.operation.distance3d;

import com.revolsys.geometry.algorithm.CGAlgorithms3D;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.operation.distance.GeometryLocation;

/**
 * Find two points on two {@link Geometry}s which lie within a given distance,
 * or else are the nearest points on the geometries (in which case this also
 * provides the distance between the geometries).
 * <p>
 * The distance computation also finds a pair of points in the input geometries
 * which have the minimum distance between them. If a point lies in the interior
 * of a line segment, the coordinate computed is a close approximation to the
 * exact point.
 * <p>
 * The algorithms used are straightforward O(n^2) comparisons. This worst-case
 * performance could be improved on by using Voronoi techniques or spatial
 * indexes.
 *
 * @version 1.7
 */
public class Distance3DOp {
  /**
   * Compute the distance between the nearest points of two geometries.
   *
   * @param g0
   *            a {@link Geometry}
   * @param g1
   *            another {@link Geometry}
   * @return the distance between the geometries
   */
  public static double distance(final Geometry g0, final Geometry g1) {
    final Distance3DOp distOp = new Distance3DOp(g0, g1);
    return distOp.distance();
  }

  /**
   * Test whether two geometries lie within a given distance of each other.
   *
   * @param g0
   *            a {@link Geometry}
   * @param g1
   *            another {@link Geometry}
   * @param distance
   *            the distance to test
   * @return true if g0.distance(g1) <= distance
   */
  public static boolean isWithinDistance(final Geometry g0, final Geometry g1,
    final double distance) {
    final Distance3DOp distOp = new Distance3DOp(g0, g1, distance);
    return distOp.distance() <= distance;
  }

  /**
   * Compute the nearest points of two geometries. The points are
   * presented in the same order as the input Geometries.
   *
   * @param g0
   *            a {@link Geometry}
   * @param g1
   *            another {@link Geometry}
   * @return the nearest points in the geometries
   */
  public static Point[] nearestPoints(final Geometry g0, final Geometry g1) {
    final Distance3DOp distOp = new Distance3DOp(g0, g1);
    return distOp.nearestPoints();
  }

  /**
   * Convenience method to Construct a new Plane3DPolygon
   * @param poly
   * @return
   */
  private static PlanarPolygon3D polyPlane(final Geometry poly) {
    return new PlanarPolygon3D((Polygon)poly);
  }

  /**
   * Computes a point at a distance along a segment
   * specified by two relatively proportional values.
   * The fractional distance along the segment is d0/(d0+d1).
   *
   * @param p0
   *            start point of the segment
   * @param p1
   *            end point of the segment
   * @param d0
   *            proportional distance from start point to computed point
   * @param d1
   *            proportional distance from computed point to end point
   * @return the computed point
   */
  private static Point segmentPoint(final Point p0, final Point p1, final double d0,
    final double d1) {
    if (d0 <= 0) {
      return p0;
    }
    if (d1 <= 0) {
      return p1;
    }

    final double f = Math.abs(d0) / (Math.abs(d0) + Math.abs(d1));
    final double intx = p0.getX() + f * (p1.getX() - p0.getX());
    final double inty = p0.getY() + f * (p1.getY() - p0.getY());
    final double intz = p0.getZ() + f * (p1.getZ() - p0.getZ());
    return new PointDoubleXYZ(intx, inty, intz);
  }

  // input
  private final Geometry[] geom;

  private boolean isDone = false;

  private double minDistance = Double.MAX_VALUE;

  // working
  private GeometryLocation[] minDistanceLocation;

  private double terminateDistance = 0.0;

  /**
   * Constructs a Distance3DOp that computes the distance and nearest points
   * between the two specified geometries.
   *
   * @param g0
   *            a Geometry
   * @param g1
   *            a Geometry
   */
  public Distance3DOp(final Geometry g0, final Geometry g1) {
    this(g0, g1, 0.0);
  }

  /**
   * Constructs a Distance3DOp that computes the distance and nearest points
   * between the two specified geometries.
   *
   * @param g0
   *            a Geometry
   * @param g1
   *            a Geometry
   * @param terminateDistance
   *            the distance on which to terminate the search
   */
  public Distance3DOp(final Geometry g0, final Geometry g1, final double terminateDistance) {
    this.geom = new Geometry[2];
    this.geom[0] = g0;
    this.geom[1] = g1;
    this.terminateDistance = terminateDistance;
  }

  private void computeMinDistance() {
    // only compute once
    if (this.minDistanceLocation != null) {
      return;
    }
    this.minDistanceLocation = new GeometryLocation[2];

    final int geomIndex = mostPolygonalIndex();
    final boolean flip = geomIndex == 0;
    computeMinDistanceMultiMulti(this.geom[geomIndex], this.geom[1 - geomIndex], flip);
  }

  private void computeMinDistance(final Geometry g0, final Geometry g1, final boolean flip) {
    if (g0 instanceof Point) {
      if (g1 instanceof Point) {
        computeMinDistancePointPoint((Point)g0, (Point)g1, flip);
        return;
      }
      if (g1 instanceof LineString) {
        computeMinDistanceLinePoint((LineString)g1, (Point)g0, !flip);
        return;
      }
      if (g1 instanceof Polygon) {
        computeMinDistancePolygonPoint(polyPlane(g1), (Point)g0, !flip);
        return;
      }
    }
    if (g0 instanceof LineString) {
      if (g1 instanceof Point) {
        computeMinDistanceLinePoint((LineString)g0, (Point)g1, flip);
        return;
      }
      if (g1 instanceof LineString) {
        computeMinDistanceLineLine((LineString)g0, (LineString)g1, flip);
        return;
      }
      if (g1 instanceof Polygon) {
        computeMinDistancePolygonLine(polyPlane(g1), (LineString)g0, !flip);
        return;
      }
    }
    if (g0 instanceof Polygon) {
      if (g1 instanceof Point) {
        computeMinDistancePolygonPoint(polyPlane(g0), (Point)g1, flip);
        return;
      }
      if (g1 instanceof LineString) {
        computeMinDistancePolygonLine(polyPlane(g0), (LineString)g1, flip);
        return;
      }
      if (g1 instanceof Polygon) {
        computeMinDistancePolygonPolygon(polyPlane(g0), (Polygon)g1, flip);
        return;
      }
    }
  }

  private void computeMinDistanceLineLine(final LineString line0, final LineString line1,
    final boolean flip) {
    // brute force approach!
    int i = 0;
    for (final Segment segment1 : line0.segments()) {
      int j = 0;
      for (final Segment segment2 : line1.segments()) {
        final Point line1point1 = segment1.getPoint(0);
        final Point line1Point2 = segment1.getPoint(1);
        final Point line2Point1 = segment2.getPoint(0);
        final Point line2Point2 = segment2.getPoint(1);
        final double distance = CGAlgorithms3D.distanceSegmentSegment(line1point1, line1Point2,
          line2Point1, line2Point2);
        if (distance < this.minDistance) {
          this.minDistance = distance;
          // TODO: compute closest pts in 3D
          final Point[] closestPt = segment1.closestPoints(segment2);
          updateDistance(distance, new GeometryLocation(line0, i, closestPt[0].newPoint2D()),
            new GeometryLocation(line1, j, closestPt[1].newPoint2D()), flip);
        }
        if (this.isDone) {
          return;
        }
        j++;
      }
      i++;
    }
  }

  private void computeMinDistanceLinePoint(final LineString line, final Point point,
    final boolean flip) {
    final Point coord = point.getPoint();
    // brute force approach!
    int i = 0;
    for (final Segment segment : line.segments()) {
      final double dist = CGAlgorithms3D.distancePointSegment(coord, segment.getPoint(0),
        segment.getPoint(1));
      if (dist < this.minDistance) {
        final Point segClosestPoint = segment.closestPoint(coord);
        updateDistance(dist, new GeometryLocation(line, i, segClosestPoint.newPoint2D()),
          new GeometryLocation(point, 0, coord), flip);
      }
      if (this.isDone) {
        return;
      }
      i++;
    }
  }

  private void computeMinDistanceMultiMulti(final Geometry g0, final Geometry g1,
    final boolean flip) {
    if (g0.isGeometryCollection()) {
      for (final Geometry part : g0.geometries()) {
        computeMinDistanceMultiMulti(part, g1, flip);
        if (this.isDone) {
          return;
        }
      }
    } else {
      // handle case of multigeom component being empty
      if (g0.isEmpty()) {
        return;
      }

      // compute planar polygon only once for efficiency
      if (g0 instanceof Polygon) {
        computeMinDistanceOneMulti(polyPlane(g0), g1, flip);
      } else {
        computeMinDistanceOneMulti(g0, g1, flip);
      }
    }
  }

  private void computeMinDistanceOneMulti(final Geometry g0, final Geometry g1,
    final boolean flip) {
    if (g1.isGeometryCollection()) {
      for (final Geometry part : g1.geometries()) {
        computeMinDistanceOneMulti(g0, part, flip);
        if (this.isDone) {
          return;
        }
      }
    } else {
      computeMinDistance(g0, g1, flip);
    }
  }

  private void computeMinDistanceOneMulti(final PlanarPolygon3D poly, final Geometry geometry,
    final boolean flip) {
    if (geometry.isGeometryCollection()) {
      for (final Geometry part : geometry.geometries()) {
        computeMinDistanceOneMulti(poly, part, flip);
        if (this.isDone) {
          return;
        }
      }
    } else {
      if (geometry instanceof Point) {
        computeMinDistancePolygonPoint(poly, (Point)geometry, flip);
        return;
      }
      if (geometry instanceof LineString) {
        computeMinDistancePolygonLine(poly, (LineString)geometry, flip);
        return;
      }
      if (geometry instanceof Polygon) {
        computeMinDistancePolygonPolygon(poly, (Polygon)geometry, flip);
        return;
      }
    }
  }

  private void computeMinDistancePointPoint(final Point point0, final Point point1,
    final boolean flip) {
    final double dist = CGAlgorithms3D.distance(point0.getPoint(), point1.getPoint());
    if (dist < this.minDistance) {
      updateDistance(dist, new GeometryLocation(point0, 0, point0.getPoint()),
        new GeometryLocation(point1, 0, point1.getPoint()), flip);
    }
  }

  private void computeMinDistancePolygonLine(final PlanarPolygon3D poly, final LineString line,
    final boolean flip) {

    // first test if line intersects polygon
    final Point intPt = intersection(poly, line);
    if (intPt != null) {
      updateDistance(0, new GeometryLocation(poly.getPolygon(), 0, intPt),
        new GeometryLocation(line, 0, intPt), flip);
      return;
    }

    // if no intersection, then compute line distance to polygon rings
    computeMinDistanceLineLine(poly.getPolygon().getShell(), line, flip);
    if (this.isDone) {
      return;
    }
    final int nHole = poly.getPolygon().getHoleCount();
    for (int i = 0; i < nHole; i++) {
      computeMinDistanceLineLine(poly.getPolygon().getHole(i), line, flip);
      if (this.isDone) {
        return;
      }
    }
  }

  private void computeMinDistancePolygonPoint(final PlanarPolygon3D polyPlane, final Point point,
    final boolean flip) {
    final Point pt = point.getPoint();

    final LineString shell = polyPlane.getPolygon().getShell();
    if (polyPlane.intersects(pt, shell)) {
      // point is either inside or in a hole

      final int nHole = polyPlane.getPolygon().getHoleCount();
      for (int i = 0; i < nHole; i++) {
        final LineString hole = polyPlane.getPolygon().getHole(i);
        if (polyPlane.intersects(pt, hole)) {
          computeMinDistanceLinePoint(hole, point, flip);
          return;
        }
      }
      // point is in interior of polygon
      // distance is distance to polygon plane
      final double dist = Math.abs(polyPlane.getPlane().orientedDistance(pt));
      updateDistance(dist, new GeometryLocation(polyPlane.getPolygon(), 0, pt),
        new GeometryLocation(point, 0, pt), flip);
    }
    // point is outside polygon, so compute distance to shell linework
    computeMinDistanceLinePoint(shell, point, flip);
  }

  /**
   * Computes distance between two polygons.
   *
   * To compute the distance, compute the distance
   * between the rings of one polygon and the other polygon,
   * and vice-versa.
   * If the polygons intersect, then at least one ring must
   * intersect the other polygon.
   * Note that it is NOT sufficient to test only the shell rings.
   * A counter-example is a "figure-8" polygon A
   * and a simple polygon B at right angles to A, with the ring of B
   * passing through the holes of A.
   * The polygons intersect,
   * but A's shell does not intersect B, and B's shell does not intersect A.
   *
   * @param poly0
   * @param poly1
   * @param geomIndex
   */
  private void computeMinDistancePolygonPolygon(final PlanarPolygon3D poly0, final Polygon poly1,
    final boolean flip) {
    computeMinDistancePolygonRings(poly0, poly1, flip);
    if (this.isDone) {
      return;
    }
    final PlanarPolygon3D polyPlane1 = new PlanarPolygon3D(poly1);
    computeMinDistancePolygonRings(polyPlane1, poly0.getPolygon(), flip);
  }

  /**
   * Compute distance between a polygon and the rings of another.
   *
   * @param poly
   * @param ringPoly
   * @param geomIndex
   */
  private void computeMinDistancePolygonRings(final PlanarPolygon3D poly, final Polygon ringPoly,
    final boolean flip) {
    // compute shell ring
    computeMinDistancePolygonLine(poly, ringPoly.getShell(), flip);
    if (this.isDone) {
      return;
    }
    // compute hole rings
    final int nHole = ringPoly.getHoleCount();
    for (int i = 0; i < nHole; i++) {
      computeMinDistancePolygonLine(poly, ringPoly.getHole(i), flip);
      if (this.isDone) {
        return;
      }
    }
  }

  /**
   * Report the distance between the nearest points on the input geometries.
   *
   * @return the distance between the geometries, or 0 if either input geometry is empty
   * @throws IllegalArgumentException
   *             if either input geometry is null
   */
  public double distance() {
    if (this.geom[0] == null || this.geom[1] == null) {
      throw new IllegalArgumentException("null geometries are not supported");
    }
    if (this.geom[0].isEmpty() || this.geom[1].isEmpty()) {
      return 0.0;
    }

    computeMinDistance();
    return this.minDistance;
  }

  private Point intersection(final PlanarPolygon3D poly, final LineString line) {
    final LineString seq = line;
    if (seq.getVertexCount() == 0) {
      return null;
    }

    // start point of line
    Point p0 = seq.getPoint(0);
    double d0 = poly.getPlane().orientedDistance(p0);

    // for each segment in the line
    for (int i = 0; i < seq.getVertexCount() - 1; i++) {
      p0 = seq.getPoint(i);
      final Point p1 = seq.getPoint(i + 1);
      final double d1 = poly.getPlane().orientedDistance(p1);

      /**
       * If the oriented distances of the segment endpoints have the same sign,
       * the segment does not cross the plane, and is skipped.
       */
      if (d0 * d1 > 0) {
        continue;
      }

      /**
       * Compute segment-plane intersection point
       * which is then used for a point-in-polygon test.
       * The endpoint distances to the plane d0 and d1
       * give the proportional distance of the intersection point
       * along the segment.
       */
      final Point intPt = segmentPoint(p0, p1, d0, d1);
      // Point intPt = polyPlane.intersection(p0, p1, s0, s1);
      if (poly.intersects(intPt)) {
        return intPt;
      }

      // shift to next segment
      d0 = d1;
    }
    return null;
  }

  /**
   * Finds the index of the "most polygonal" input geometry.
   * This optimizes the computation of the best-fit plane,
   * since it is cached only for the left-hand geometry.
   *
   * @return the index of the most polygonal geometry
   */
  private int mostPolygonalIndex() {
    final Dimension dim0 = this.geom[0].getDimension();
    final Dimension dim1 = this.geom[1].getDimension();
    if (dim0.isArea() && dim1.isArea()) {
      if (this.geom[0].getVertexCount() > this.geom[1].getVertexCount()) {
        return 0;
      }
      return 1;
    }
    // no more than one is dim 2
    if (dim0.isArea()) {
      return 0;
    }
    if (dim1.isArea()) {
      return 1;
    }
    // both dim <= 1 - don't flip
    return 0;
  }

  /**
   * Report the locations of the nearest points in the input geometries. The
   * locations are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link GeometryLocation}s for the nearest points
   */
  public GeometryLocation[] nearestLocations() {
    computeMinDistance();
    return this.minDistanceLocation;
  }

  /**
   * Report the coordinates of the nearest points in the input geometries. The
   * points are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link Coordinates}s of the nearest points
   */
  public Point[] nearestPoints() {
    computeMinDistance();
    final Point[] nearestPts = new Point[] {
      this.minDistanceLocation[0].getPoint(), this.minDistanceLocation[1].getPoint()
    };
    return nearestPts;
  }

  private void updateDistance(final double dist, final GeometryLocation loc0,
    final GeometryLocation loc1, final boolean flip) {
    this.minDistance = dist;
    final int index = flip ? 1 : 0;
    this.minDistanceLocation[index] = loc0;
    this.minDistanceLocation[1 - index] = loc1;
    if (this.minDistance <= this.terminateDistance) {
      this.isDone = true;
    }
  }

}
