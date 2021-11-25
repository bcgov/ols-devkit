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

package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.Assert;
import com.revolsys.geometry.util.Triangles;

/**
 * Computes the <b>Minimum Bounding Circle</b> (MBC)
 * for the points in a {@link Geometry}.
 * The MBC is the smallest circle which <tt>cover</tt>s
 * all the input points
 * (this is also known as the <b>Smallest Enclosing Circle</b>).
 * This is equivalent to computing the Maximum Diameter
 * of the input point set.
 * <p>
 * The computed circle can be specified in two equivalent ways,
 * both of which are provide as output by this class:
 * <ul>
 * <li>As a centre point and a radius
 * <li>By the set of points defining the circle.
 * Depending on the number of points in the input
 * and their relative positions, this
 * will be specified by anywhere from 0 to 3 points.
 * <ul>
 * <li>0 or 1 points indicate an empty or trivial input point arrangement.
 * <li>2 or 3 points define a circle which contains
 * all the input points.
 * </ul>
 * </ul>
 * The class can also output a {@link Geometry} which approximates the
 * shape of the MBC (although as an approximation
 * it is <b>not</b> guaranteed to <tt>cover</tt> all the input points.)
 *
 * @author Martin Davis
 *
 * @see MinimumDiameter
 *
 */
public class MinimumBoundingCircle {
  /*
   * The algorithm used is based on the one by Jon Rokne in the article
   * "An Easy Bounding Circle" in <i>Graphic Gems II</i>.
   */

  /**
   * Creates a deep copy of the argument {@link Coordinates} array.
   *
   * @param points an array of Coordinates
   * @return a deep copy of the input
   */
  public static Point[] copyDeep(final Point[] points) {
    final Point[] copy = new Point[points.length];
    int i = 0;
    for (final Point point : points) {
      copy[i++] = point.newPoint();
    }
    return copy;
  }

  /**
   * Creates a deep copy of a given section of a source {@link Coordinates} array
   * into a destination Point array.
   * The destination array must be an appropriate size to receive
   * the copied coordinates.
   *
   * @param src an array of Coordinates
   * @param srcStart the index to start copying from
   * @param dest the
   * @param destStart the destination index to start copying to
   * @param length the number of items to copy
   */
  public static void copyDeep(final Point[] src, final int srcStart, final Point[] dest,
    final int destStart, final int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = src[srcStart + i].newPoint();
    }
  }

  private static Point lowestPoint(final Point[] pts) {
    Point min = pts[0];
    for (int i = 1; i < pts.length; i++) {
      if (pts[i].getY() < min.getY()) {
        min = pts[i];
      }
    }
    return min;
  }

  private static Point pointWithMinAngleWithSegment(final Point[] pts, final Point P,
    final Point Q) {
    double minAng = Double.MAX_VALUE;
    Point minAngPt = null;
    for (final Point p : pts) {

      if (p == P) {
        continue;
      }
      if (p == Q) {
        continue;
      }

      final double ang = Point.angleBetween(P, p, Q);
      if (ang < minAng) {
        minAng = ang;
        minAngPt = p;
      }
    }
    return minAngPt;

  }

  private static Point pointWitMinAngleWithX(final Point[] pts, final Point P) {
    double minSin = Double.MAX_VALUE;
    Point minAngPt = null;
    for (final Point p : pts) {

      if (p == P) {
        continue;
      }

      /**
       * The sin of the angle is a simpler proxy for the angle itself
       */
      final double dx = p.getX() - P.getX();
      double dy = p.getY() - P.getY();
      if (dy < 0) {
        dy = -dy;
      }
      final double len = Math.sqrt(dx * dx + dy * dy);
      final double sin = dy / len;

      if (sin < minSin) {
        minSin = sin;
        minAngPt = p;
      }
    }
    return minAngPt;
  }

  private Point centre = null;

  private Point[] extremalPts = null;

  private final Geometry geometry;

  private double radius = 0.0;

  /**
   * Creates a new object for computing the minimum bounding circle for the
   * point set defined by the vertices of the given geometry.
   *
   * @param geometry the geometry to use to obtain the point set
   */
  public MinimumBoundingCircle(final Geometry geometry) {
    this.geometry = geometry;
  }

  private void compute() {
    if (this.extremalPts != null) {
      return;
    }

    computeCirclePoints();
    computeCentre();
    if (this.centre != null) {
      this.radius = this.centre.distancePoint(this.extremalPts[0]);
    }
  }

  private void computeCentre() {
    switch (this.extremalPts.length) {
      case 0:
        this.centre = null;
      break;
      case 1:
        this.centre = this.extremalPts[0];
      break;
      case 2:
        this.centre = new PointDoubleXY(
          (this.extremalPts[0].getX() + this.extremalPts[1].getX()) / 2.0,
          (this.extremalPts[0].getY() + this.extremalPts[1].getY()) / 2.0);
      break;
      case 3:
        this.centre = Triangles.circumcentre(this.extremalPts[0], this.extremalPts[1],
          this.extremalPts[2]);
      break;
    }
  }

  private void computeCirclePoints() {
    // handle degenerate or trivial cases
    if (this.geometry.isEmpty()) {
      this.extremalPts = new Point[0];
      return;
    }
    if (this.geometry.getVertexCount() == 1) {
      this.extremalPts = new Point[] {
        this.geometry.getPoint()
      };
      return;
    }

    /**
     * The problem is simplified by reducing to the convex hull.
     * Computing the convex hull also has the useful effect of eliminating duplicate points
     */
    final Geometry convexHull = this.geometry.convexHull();

    final Point[] hullPts = CoordinatesListUtil.getPointArray(convexHull);

    // strip duplicate final point, if any
    Point[] pts = hullPts;
    if (hullPts[0].equals(2, hullPts[hullPts.length - 1])) {
      pts = new Point[hullPts.length - 1];
      MinimumBoundingCircle.copyDeep(hullPts, 0, pts, 0, hullPts.length - 1);
    }

    /**
     * Optimization for the trivial case where the CH has fewer than 3 points
     */
    if (pts.length <= 2) {
      this.extremalPts = MinimumBoundingCircle.copyDeep(pts);
      return;
    }

    // find a point P with minimum Y ordinate
    Point P = lowestPoint(pts);

    // find a point Q such that the angle that PQ makes with the x-axis is
    // minimal
    Point Q = pointWitMinAngleWithX(pts, P);

    /**
     * Iterate over the remaining points to find
     * a pair or triplet of points which determine the minimal circle.
     * By the design of the algorithm,
     * at most <tt>pts.length</tt> iterations are required to terminate
     * with a correct result.
     */
    for (final Point point : pts) {
      final Point R = pointWithMinAngleWithSegment(pts, P, Q);

      // if PRQ is obtuse, then MBC is determined by P and Q
      if (Point.isObtuse(P, R, Q)) {
        this.extremalPts = new Point[] {
          P, Q
        };
        return;
      }
      if (Point.isObtuse(R, P, Q)) {
        // if RPQ is obtuse, update baseline and iterate
        P = R;
      } else if (Point.isObtuse(R, Q, P)) {
        // if RQP is obtuse, update baseline and iterate
        Q = R;
      } else {
        // otherwise all angles are acute, and the MBC is determined by the
        // triangle PQR
        this.extremalPts = new Point[] {
          P, Q, R
        };
        return;
      }
    }
    Assert.shouldNeverReachHere("Logic failure in Minimum Bounding Circle algorithm!");
  }

  /**
   * Gets the centre point of the computed Minimum Bounding Circle.
   *
   * @return the centre point of the Minimum Bounding Circle
   * @return null if the input is empty
   */
  public Point getCentre() {
    compute();
    return this.centre;
  }

  /**
   * Gets a geometry which represents the Minimum Bounding Circle.
   * If the input is degenerate (empty or a single unique point),
   * this method will return an empty geometry or a single Point geometry.
   * Otherwise, a Polygon will be returned which approximates the
   * Minimum Bounding Circle.
   * (Note that because the computed polygon is only an approximation,
   * it may not precisely contain all the input points.)
   *
   * @return a Geometry representing the Minimum Bounding Circle.
   */
  public Geometry getCircle() {
    // TODO: ensure the output circle contains the extermal points.
    // TODO: or maybe even ensure that the returned geometry contains ALL the
    // input points?

    compute();
    if (this.centre == null) {
      return this.geometry.getGeometryFactory().polygon();
    } else {
      final Point centrePoint = this.geometry.getGeometryFactory().point(this.centre);
      if (this.radius == 0.0) {
        return centrePoint;
      }
      return centrePoint.buffer(this.radius);
    }
  }

  /**
   * Gets the extremal points which define the computed Minimum Bounding Circle.
   * There may be zero, one, two or three of these points,
   * depending on the number of points in the input
   * and the geometry of those points.
   *
   * @return the points defining the Minimum Bounding Circle
   */
  public Point[] getExtremalPoints() {
    compute();
    return this.extremalPts;
  }

  /**
   * Gets the radius of the computed Minimum Bounding Circle.
   *
   * @return the radius of the Minimum Bounding Circle
   */
  public double getRadius() {
    compute();
    return this.radius;
  }
}
