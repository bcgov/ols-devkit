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
package com.revolsys.geometry.operation.distance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.segment.Segment;

/**
 * Find two points on two {@link Geometry}s which lie
 * within a given distance, or else are the nearest points
 * on the geometries (in which case this also
 * provides the distance between the geometries).
 * <p>
 * The distance computation also finds a pair of points in the input geometries
 * which have the minimum distance between them.
 * If a point lies in the interior of a line segment,
 * the coordinate computed is a close
 * approximation to the exact point.
 * <p>
 * The algorithms used are straightforward O(n^2)
 * comparisons.  This worst-case performance could be improved on
 * by using Voronoi techniques or spatial indexes.
 *
 * @version 1.7
 */
public class DistanceWithLocation {

  /**
   * Compute the distance between the nearest points of two geometries.
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @return the distance between the geometries
   */
  public static double distance(final Geometry geometry1, final Geometry geometry2) {
    final DistanceWithLocation distOp = new DistanceWithLocation(geometry1, geometry2);
    return distOp.distance();
  }

  /**
   * Test whether two geometries lie within a given distance of each other.
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @param distance the distance to test
   * @return true if geometry1.distance(geometry2) <= distance
   */
  public static boolean isWithinDistance(final Geometry geometry1, final Geometry geometry2,
    final double distance) {
    final DistanceWithLocation distOp = new DistanceWithLocation(geometry1, geometry2, distance);
    return distOp.distance() <= distance;
  }

  /**
   * Compute the nearest points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @return the nearest points in the geometries
   */
  public static List<Point> nearestPoints(final Geometry geometry1, final Geometry geometry2) {
    final DistanceWithLocation distOp = new DistanceWithLocation(geometry1, geometry2);
    return distOp.nearestPoints();
  }

  private boolean computed = false;

  private final Geometry geometry1;

  private final Geometry geometry2;

  private double minDistance = Double.MAX_VALUE;

  private GeometryLocation minDistanceLocation1;

  private GeometryLocation minDistanceLocation2;

  // working
  private final PointLocator pointLocator = new PointLocator();

  private double terminateDistance = 0.0;

  /**
   * Constructs a DistanceWithLocation that computes the distance and nearest points between
   * the two specified geometries.
   * @param geometry1 a Geometry
   * @param geometry2 a Geometry
   */
  public DistanceWithLocation(final Geometry geometry1, final Geometry geometry2) {
    this(geometry1, geometry2, 0.0);
  }

  /**
   * Constructs a DistanceWithLocation that computes the distance and nearest points between
   * the two specified geometries.
   * @param geometry1 a Geometry
   * @param geometry2 a Geometry
   * @param terminateDistance the distance on which to terminate the search
   */
  public DistanceWithLocation(final Geometry geometry1, final Geometry geometry2,
    final double terminateDistance) {
    if (geometry1 == null || geometry2 == null) {
      throw new IllegalArgumentException("null geometries are not supported");
    }
    this.geometry1 = geometry1;
    this.geometry2 = geometry2;
    this.terminateDistance = terminateDistance;
  }

  private void computeContainmentDistance() {
    final GeometryLocation[] locPtPoly = new GeometryLocation[2];
    // test if either geometry has a vertex inside the other
    computeContainmentDistance(0, locPtPoly);
    if (this.minDistance > this.terminateDistance) {
      computeContainmentDistance(1, locPtPoly);
    }
  }

  private void computeContainmentDistance(final GeometryLocation ptLoc, final Polygon poly,
    final GeometryLocation[] locPtPoly) {
    final Point pt = ptLoc.getPoint();
    final double x = pt.getX();
    final double y = pt.getY();
    // if pt is not in exterior, distance to geom is 0
    if (Location.EXTERIOR != poly.locate(x, y)) {
      this.minDistance = 0.0;
      locPtPoly[0] = ptLoc;
      locPtPoly[1] = new GeometryLocation(poly, pt);
    }
  }

  private void computeContainmentDistance(final int polyGeomIndex,
    final GeometryLocation[] locPtPoly) {
    Geometry geometry1;
    Geometry geometry2;
    if (polyGeomIndex == 0) {
      geometry1 = this.geometry1;
      geometry2 = this.geometry2;
    } else {
      geometry1 = this.geometry2;
      geometry2 = this.geometry1;
    }
    final boolean flip = polyGeomIndex == 0;
    final List<Polygon> polys = geometry1.getGeometries(Polygon.class);
    if (polys.size() > 0) {
      final List<GeometryLocation> insideLocs = ConnectedElementLocationFilter
        .getLocations(geometry2);
      computeContainmentDistance(insideLocs, polys, locPtPoly);
      if (this.minDistance <= this.terminateDistance) {
        // this assignment is determined by the order of the args in the
        // computeInside call above
        setMinDistanceLocations(locPtPoly[0], locPtPoly[1], flip);
        return;
      }
    }
  }

  private void computeContainmentDistance(final List<GeometryLocation> locations,
    final List<Polygon> polygons, final GeometryLocation[] locPtPoly) {
    for (final GeometryLocation loc : locations) {
      for (final Polygon polygon : polygons) {
        computeContainmentDistance(loc, polygon, locPtPoly);
        if (this.minDistance <= this.terminateDistance) {
          return;
        }
      }
    }
  }

  /**
   * Computes distance between facets (lines and points)
   * of input geometries.
   *
   */
  private void computeFacetDistance() {
    /**
     * Geometries are not wholly inside, so compute distance from lines and points
     * of one to lines and points of the other
     */
    final List<LineString> lines0 = this.geometry1.getGeometryComponents(LineString.class);
    final List<LineString> lines1 = this.geometry2.getGeometryComponents(LineString.class);

    if (!computeLinesLines(lines0, lines1)) {
      final List<Point> points1 = this.geometry2.getGeometries(Point.class);
      if (!computeLinesPoints(lines0, points1)) {
        final List<Point> points0 = this.geometry1.getGeometries(Point.class);
        if (!computePointsLines(points0, lines1)) {
          computePointsPoints(points0, points1);
        }
      }
    }
  }

  private boolean computeLineLine(final LineString line1, final LineString line2) {
    if (this.minDistance == Double.MAX_VALUE || line1.bboxDistance(line2) <= this.minDistance) {
      for (final Segment segment1 : line1.segments()) {
        for (final Segment segment2 : line2.segments()) {
          final double dist = segment1.distance(segment2);
          if (dist < this.minDistance) {
            this.minDistance = dist;
            final Point[] closestPt = segment1.closestPoints(segment2);
            final int segmentIndex1 = segment1.getSegmentIndex();
            this.minDistanceLocation1 = new GeometryLocation(line1, segmentIndex1, closestPt[0]);
            final int segmentIndex2 = segment2.getSegmentIndex();
            this.minDistanceLocation2 = new GeometryLocation(line2, segmentIndex2, closestPt[1]);
            if (this.minDistance <= this.terminateDistance) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean computeLinePoint(final LineString line, final Point point) {
    if (this.minDistance == Double.MAX_VALUE
      || line.getBoundingBox().bboxDistance(point) <= this.minDistance) {
      for (final Segment segment : line.segments()) {
        final double distance = segment.distancePoint(point);
        if (distance < this.minDistance) {
          this.minDistance = distance;
          final Point closestPoint = segment.closestPoint(point);
          final int segmentIndex = segment.getSegmentIndex();
          this.minDistanceLocation1 = new GeometryLocation(line, segmentIndex, closestPoint);
          this.minDistanceLocation2 = new GeometryLocation(point, 0, point);
          if (this.minDistance <= this.terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean computeLinesLines(final List<LineString> lines1, final List<LineString> lines2) {
    for (final LineString line1 : lines1) {
      for (final LineString line2 : lines2) {
        if (computeLineLine(line1, line2)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computeLinesPoints(final List<LineString> lines, final List<Point> points) {
    for (final LineString line : lines) {
      for (final Point point : points) {
        if (computeLinePoint(line, point)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computePointLine(final Point point, final LineString line) {
    final BoundingBox boundingBox = line.getBoundingBox();
    if (this.minDistance == Double.MAX_VALUE
      || boundingBox.bboxDistance(point) <= this.minDistance) {
      for (final Segment segment : line.segments()) {
        final double distance = segment.distancePoint(point);
        if (distance < this.minDistance) {
          this.minDistance = distance;
          final Point closestPoint = segment.closestPoint(point);
          final int segmentIndex = segment.getSegmentIndex();
          this.minDistanceLocation1 = new GeometryLocation(point, 0, point);
          this.minDistanceLocation2 = new GeometryLocation(line, segmentIndex, closestPoint);
          if (this.minDistance <= this.terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean computePointsLines(final List<Point> points, final List<LineString> lines) {
    for (final Point point : points) {
      for (final LineString line : lines) {
        if (computePointLine(point, line)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computePointsPoints(final List<Point> points1, final List<Point> points2) {
    for (final Point point1 : points1) {
      for (final Point point2 : points2) {
        final double dist = point1.distancePoint(point2);
        if (dist < this.minDistance) {
          this.minDistance = dist;
          this.minDistanceLocation1 = new GeometryLocation(point1, 0, point1);
          this.minDistanceLocation2 = new GeometryLocation(point2, 0, point2);
          if (this.minDistance <= this.terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Report the distance between the nearest points on the input geometries.
   *
   * @return the distance between the geometries
   * or 0 if either input geometry is empty
   * @throws IllegalArgumentException if either input geometry is null
   */
  public double distance() {
    if (!this.computed) {
      if (this.geometry1.isEmpty() || this.geometry2.isEmpty()) {
        this.minDistance = 0;
      } else {
        this.computed = true;
        computeContainmentDistance();
        if (this.minDistance > this.terminateDistance) {
          computeFacetDistance();
        }
      }
    }
    return this.minDistance;
  }

  /**
   * Report the locations of the nearest points in the input geometries.
   * The locations are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link GeometryLocation}s for the nearest points
   */
  public GeometryLocation[] nearestLocations() {
    distance();
    if (this.minDistanceLocation1 == null) {
      return null;
    } else {
      return new GeometryLocation[] {
        this.minDistanceLocation1, this.minDistanceLocation2
      };
    }
  }

  /**
   * Report the coordinates of the nearest points in the input geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link Coordinates}s of the nearest points
   */
  public List<Point> nearestPoints() {
    distance();
    if (this.minDistanceLocation1 == null) {
      return Collections.emptyList();
    } else {
      final Point point1 = this.minDistanceLocation1.getPoint();
      final Point point2 = this.minDistanceLocation2.getPoint();
      return Arrays.asList(point1, point2);
    }
  }

  private void setMinDistanceLocations(final GeometryLocation location1,
    final GeometryLocation location2, final boolean flip) {
    if (location1 != null) {
      if (flip) {
        this.minDistanceLocation1 = location2;
        this.minDistanceLocation2 = location1;
      } else {
        this.minDistanceLocation1 = location1;
        this.minDistanceLocation2 = location2;
      }
    }
  }
}
