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

import java.util.List;

import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

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
public class DistanceOp {
  private boolean computed = false;

  private final Geometry geometry1;

  private final Geometry geometry2;

  private double minDistance = Double.MAX_VALUE;

  // working
  private final PointLocator pointLocator = new PointLocator();

  private double terminateDistance = 0.0;

  /**
   * Constructs a DistanceOp that computes the distance and nearest points between
   * the two specified geometries.
   * @param geometry1 a Geometry
   * @param geometry2 a Geometry
   * @param terminateDistance the distance on which to terminate the search
   */
  public DistanceOp(final Geometry geometry1, final Geometry geometry2,
    final double terminateDistance) {
    if (geometry1 == null || geometry2 == null) {
      throw new IllegalArgumentException("null geometries are not supported");
    }
    this.geometry1 = geometry1;
    this.geometry2 = geometry2;
    this.terminateDistance = terminateDistance;
  }

  private boolean computeContainmentDistance() {
    if (computeContainmentDistance(this.geometry1, this.geometry2)) {
      return true;
    } else if (computeContainmentDistance(this.geometry2, this.geometry1)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean computeContainmentDistance(final Geometry geometry1, final Geometry geometry2) {
    final List<Polygon> polys = geometry1.getGeometries(Polygon.class);
    if (polys.size() > 0) {
      final List<Point> insidePoints = ConnectedElementLocationFilter.getPoints(geometry2);
      if (computeContainmentDistance(insidePoints, polys)) {
        return true;
      }
    }
    return false;
  }

  private boolean computeContainmentDistance(final List<Point> points,
    final List<Polygon> polygons) {
    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      for (final Polygon polygon : polygons) {
        if (computeContainmentDistance(polygon, x, y)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computeContainmentDistance(final Polygon polygon, final double x,
    final double y) {
    // if pt is not in exterior, distance to geom is 0
    if (Location.EXTERIOR != polygon.locate(x, y)) {
      this.minDistance = 0.0;
      return true;
    } else {
      return false;
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
        if (!computeLinesPoints(lines1, points0)) {
          computePointsPoints(points0, points1);
        }
      }
    }
  }

  private boolean computeLineLine(final LineString line1, final LineString line2) {
    if (this.minDistance == Double.MAX_VALUE || line1.bboxDistance(line2) <= this.minDistance) {
      final double distance = line1.distanceLine(line2);
      if (distance < this.minDistance) {
        this.minDistance = distance;
        if (this.minDistance <= this.terminateDistance) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computeLinePoint(final LineString line, final Point point) {
    if (this.minDistance == Double.MAX_VALUE
      || line.getBoundingBox().bboxDistance(point) <= this.minDistance) {
      final double distance = line.distancePoint(point, this.terminateDistance);
      if (distance < this.minDistance) {
        this.minDistance = distance;
        if (this.minDistance <= this.terminateDistance) {
          return true;
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

  private boolean computePointsPoints(final List<Point> points1, final List<Point> points2) {
    for (final Point point1 : points1) {
      for (final Point point2 : points2) {
        final double distance = point1.distancePoint(point2);
        if (distance < this.minDistance) {
          this.minDistance = distance;
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
        if (!computeContainmentDistance()) {
          computeFacetDistance();
        }
      }
    }
    return this.minDistance;
  }

}
