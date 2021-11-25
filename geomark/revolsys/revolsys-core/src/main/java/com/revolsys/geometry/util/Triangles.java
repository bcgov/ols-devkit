package com.revolsys.geometry.util;

import com.revolsys.geometry.algorithm.HCoordinate;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

public interface Triangles {

  /**
   * Computes the point at which the bisector of the angle ABC cuts the segment
   * AC.
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the angle bisector cut point
   */
  static Point angleBisector(final Point a, final Point b, final Point c) {
    /**
     * Uses the fact that the lengths of the parts of the split segment are
     * proportional to the lengths of the adjacent triangle sides
     */
    final double len0 = b.distancePoint(a);
    final double len2 = b.distancePoint(c);
    final double frac = len0 / (len0 + len2);
    final double dx = c.getX() - a.getX();
    final double dy = c.getY() - a.getY();

    final Point splitPt = new PointDoubleXY(a.getX() + frac * dx, a.getY() + frac * dy);
    return splitPt;
  }

  /**
   * Computes the 2D area of a triangle. The area value is always non-negative.
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param x3
   * @param y3
   * @return The area.
   */
  static double area(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    return Math.abs(((x3 - x1) * (y2 - y1) - (x2 - x1) * (y3 - y1)) / 2);
  }

  /**
   * Computes the 2D area of a triangle. The area value is always non-negative.
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the area of the triangle
   *
   * @see #signedArea(Coordinate, Coordinate, Coordinate)
   */
  static double area(final Point a, final Point b, final Point c) {
    final double x1 = a.getX();
    final double y1 = a.getY();
    final double x2 = b.getX();
    final double y2 = b.getY();
    final double x3 = c.getX();
    final double y3 = c.getY();
    return area(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Returns twice the signed area of the triangle p1-p2-p3.
   * The area is positive if the triangle is oriented CCW, and negative if CW.
   */
  static double area2(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    return (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
  }

  /**
   * Computes the 3D area of a triangle. The area value is always non-negative.
   *
   * Uses the formula 1/2 * | u x v | where u,v are the side vectors of the
  * triangle x is the vector cross-product
     * @return The area.
   */
  static double area3D(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final double x3, final double y3, final double z3) {
    // side vectors u and v
    final double ux = x2 - x1;
    final double uy = y2 - y1;
    final double uz = z2 - z1;

    final double vx = x3 - x1;
    final double vy = y3 - y1;
    final double vz = z3 - z1;

    // cross-product = u x v
    final double crossx = uy * vz - uz * vy;
    final double crossy = uz * vx - ux * vz;
    final double crossz = ux * vy - uy * vx;

    // tri area = 1/2 * | u x v |
    final double absSq = crossx * crossx + crossy * crossy + crossz * crossz;
    final double area3D = Math.sqrt(absSq) / 2;
    return area3D;
  }

  /**
   * Computes the 3D area of a triangle. The value computed is always
   * non-negative.
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the 3D area of the triangle
   */
  static double area3D(final Point a, final Point b, final Point c) {
    final double x1 = a.getX();
    final double x2 = b.getX();
    final double z1 = a.getZ();
    final double y1 = a.getY();
    final double y2 = b.getY();
    final double z2 = b.getZ();
    final double x3 = c.getX();
    final double y3 = c.getY();
    final double z3 = c.getZ();
    return area3D(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }

  /**
   * Computes the centroid (centre of mass) of a triangle. This is also the
   * point at which the triangle's three medians intersect (a triangle median is
   * the segment from a vertex of the triangle to the midpoint of the opposite
   * side). The centroid divides each median in a ratio of 2:1.
   * <p>
   * The centroid always lies within the triangle.
   *
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the centroid of the triangle
   */
  static Point centroid(final Point a, final Point b, final Point c) {
    final double x = (a.getX() + b.getX() + c.getX()) / 3;
    final double y = (a.getY() + b.getY() + c.getY()) / 3;
    return new PointDoubleXY(x, y);
  }

  /**
   * Computes the circumcentre of a triangle. The circumcentre is the centre of
   * the circumcircle, the smallest circle which encloses the triangle. It is
   * also the common intersection point of the perpendicular bisectors of the
   * sides of the triangle, and is the only point which has equal distance to
   * all three vertices of the triangle.
   * <p>
   * The circumcentre does not necessarily lie within the triangle. For example,
   * the circumcentre of an obtuse isoceles triangle lies outside the triangle.
   * <p>
   * This method uses an algorithm due to J.R.Shewchuk which uses normalization
   * to the origin to improve the accuracy of computation. (See <i>Lecture Notes
   * on Geometric Robustness</i>, Jonathan Richard Shewchuk, 1999).
   *
   * @param a
   *          a vertx of the triangle
   * @param b
   *          a vertx of the triangle
   * @param c
   *          a vertx of the triangle
   * @return the circumcentre of the triangle
   */
  static Point circumcentre(final Point a, final Point b, final Point c) {
    final double cx = c.getX();
    final double cy = c.getY();
    final double ax = a.getX() - cx;
    final double ay = a.getY() - cy;
    final double bx = b.getX() - cx;
    final double by = b.getY() - cy;

    final double denom = 2 * det(ax, ay, bx, by);
    final double numx = det(ay, ax * ax + ay * ay, by, bx * bx + by * by);
    final double numy = det(ax, ax * ax + ay * ay, bx, bx * bx + by * by);

    final double ccx = cx - numx / denom;
    final double ccy = cy + numy / denom;

    return new PointDoubleXY(ccx, ccy);
  }

  /**
   * Computes the determinant of a 2x2 matrix. Uses standard double-precision
   * arithmetic, so is susceptible to round-off error.
   *
   * @param m00
   *          the [0,0] entry of the matrix
   * @param m01
   *          the [0,1] entry of the matrix
   * @param m10
   *          the [1,0] entry of the matrix
   * @param m11
   *          the [1,1] entry of the matrix
   * @return the determinant
   */
  static double det(final double m00, final double m01, final double m10, final double m11) {
    return m00 * m11 - m01 * m10;
  }

  /**
   * Computes the incentre of a triangle. The <i>inCentre</i> of a triangle is
   * the point which is equidistant from the sides of the triangle. It is also
   * the point at which the bisectors of the triangle's angles meet. It is the
   * centre of the triangle's <i>incircle</i>, which is the unique circle that
   * is tangent to each of the triangle's three sides.
   * <p>
   * The incentre always lies within the triangle.
   *
   * @param a
   *          a vertx of the triangle
   * @param b
   *          a vertx of the triangle
   * @param c
   *          a vertx of the triangle
   * @return the point which is the incentre of the triangle
   */
  static Point inCentre(final Point a, final Point b, final Point c) {
    // the lengths of the sides, labelled by their opposite vertex
    final double len0 = b.distancePoint(c);
    final double len1 = a.distancePoint(c);
    final double len2 = a.distancePoint(b);
    final double circum = len0 + len1 + len2;

    final double inCentreX = (len0 * a.getX() + len1 * b.getX() + len2 * c.getX()) / circum;
    final double inCentreY = (len0 * a.getY() + len1 * b.getY() + len2 * c.getY()) / circum;
    return new PointDoubleXY(inCentreX, inCentreY);
  }

  /**
   * Computes the Z-value (elevation) of an XY point on a three-dimensional
   * plane defined by a triangle whose vertices have Z-values. The defining
   * triangle must not be degenerate (in other words, the triangle must enclose
   * a non-zero area), and must not be parallel to the Z-axis.
   * <p>
   * This method can be used to interpolate the Z-value of a point inside a
   * triangle (for example, of a TIN facet with elevations on the vertices).
   *
   * @param p
   *          the point to compute the Z-value of
   * @param v0
   *          a vertex of a triangle, with a Z ordinate
   * @param v1
   *          a vertex of a triangle, with a Z ordinate
   * @param v2
   *          a vertex of a triangle, with a Z ordinate
   * @return the computed Z-value (elevation) of the point
   */
  static double interpolateZ(final Point p, final Point v0, final Point v1, final Point v2) {
    final double x0 = v0.getX();
    final double y0 = v0.getY();
    final double a = v1.getX() - x0;
    final double b = v2.getX() - x0;
    final double c = v1.getY() - y0;
    final double d = v2.getY() - y0;
    final double det = a * d - b * c;
    final double dx = p.getX() - x0;
    final double dy = p.getY() - y0;
    final double t = (d * dx - b * dy) / det;
    final double u = (-c * dx + a * dy) / det;
    final double z = v0.getZ() + t * (v1.getZ() - v0.getZ()) + u * (v2.getZ() - v0.getZ());
    return z;
  }

  /**
   * Tests whether a triangle is acute. A triangle is acute iff all interior
   * angles are acute. This is a strict test - right triangles will return
   * <tt>false</tt> A triangle which is not acute is either right or obtuse.
   * <p>
   * Note: this implementation is not robust for angles very close to 90
   * degrees.
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return true if the triangle is acute
   */
  static boolean isAcute(final Point a, final Point b, final Point c) {
    if (!Point.isAcute(a, b, c)) {
      return false;
    }
    if (!Point.isAcute(b, c, a)) {
      return false;
    }
    if (!Point.isAcute(c, a, b)) {
      return false;
    }
    return true;
  }

  static boolean isInCircleNormalized(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3, final double x, final double y) {
    final double adx = x1 - x;
    final double ady = y1 - y;
    final double bdx = x2 - x;
    final double bdy = y2 - y;
    final double cdx = x3 - x;
    final double cdy = y3 - y;

    final double abdet = adx * bdy - bdx * ady;
    final double bcdet = bdx * cdy - cdx * bdy;
    final double cadet = cdx * ady - adx * cdy;
    final double alift = adx * adx + ady * ady;
    final double blift = bdx * bdx + bdy * bdy;
    final double clift = cdx * cdx + cdy * cdy;

    final double disc = alift * bcdet + blift * cadet + clift * abdet;
    return disc > 0;
  }

  /**
   * Computes the length of the longest side of a triangle
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the length of the longest side of the triangle
   */
  static double longestSideLength(final Point a, final Point b, final Point c) {
    final double lenAB = a.distancePoint(b);
    final double lenBC = b.distancePoint(c);
    final double lenCA = c.distancePoint(a);
    double maxLen = lenAB;
    if (lenBC > maxLen) {
      maxLen = lenBC;
    }
    if (lenCA > maxLen) {
      maxLen = lenCA;
    }
    return maxLen;
  }

  /**
   * Computes the line which is the perpendicular bisector of the line segment
   * a-b.
   *
   * @param a
   *          a point
   * @param b
   *          another point
   * @return the perpendicular bisector, as an HCoordinate
   */
  static HCoordinate perpendicularBisector(final Point a, final Point b) {
    // returns the perpendicular bisector of the line segment ab
    final double dx = b.getX() - a.getX();
    final double dy = b.getY() - a.getY();
    final HCoordinate l1 = new HCoordinate(a.getX() + dx / 2.0, a.getY() + dy / 2.0, 1.0);
    final HCoordinate l2 = new HCoordinate(a.getX() - dy + dx / 2.0, a.getY() + dx + dy / 2.0, 1.0);
    return new HCoordinate(l1, l2);
  }

  /**
   * Computes the signed 2D area of a triangle. The area value is positive if
   * the triangle is oriented CW, and negative if it is oriented CCW.
   * <p>
   * The signed area value can be used to determine point orientation, but the
   * implementation in this method is susceptible to round-off errors.
   *
   * @param a
   *          a vertex of the triangle
   * @param b
   *          a vertex of the triangle
   * @param c
   *          a vertex of the triangle
   * @return the signed 2D area of the triangle
   */
  static double signedArea(final Point a, final Point b, final Point c) {
    /**
     * Uses the formula 1/2 * | u x v | where u,v are the side vectors of the
     * triangle x is the vector cross-product For 2D vectors, this formual
     * simplifies to the expression below
     */
    return ((c.getX() - a.getX()) * (b.getY() - a.getY())
      - (b.getX() - a.getX()) * (c.getY() - a.getY())) / 2;
  }

}
