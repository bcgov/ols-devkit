package com.revolsys.geometry.algorithm;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

/**
 * Compares {@link Coordinates}s for their angle and distance
 * relative to an origin.
 *
 * @author Martin Davis
 * @version 1.7
 */
class RadialComparator implements Comparator<Point> {

  private final double originX;

  private final double originY;

  public RadialComparator(final double originX, final double originY) {
    this.originX = originX;
    this.originY = originY;
  }

  /**
   * Given two points p and q compare them with respect to their radial
   * ordering about point o.  First checks radial ordering.
   * If points are collinear, the comparison is based
   * on their distance to the origin.
   * <p>
   * p < q iff
   * <ul>
   * <li>ang(o-p) < ang(o-q) (e.g. o-p-q is CCW)
   * <li>or ang(o-p) == ang(o-q) && dist(o,p) < dist(o,q)
   * </ul>
   *
   * @param o the origin
   * @param p a point
   * @param q another point
   * @return -1, 0 or 1 depending on whether p is less than,
   * equal to or greater than q
   */
  @Override
  public int compare(final Point p1, final Point p2) {
    final double originX = this.originX;
    final double originY = this.originY;
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();

    final double dxp = x1 - originX;
    final double dyp = y1 - originY;
    final double dxq = x2 - originX;
    final double dyq = y2 - originY;

    final int orient = CGAlgorithmsDD.orientationIndex(originX, originY, x1, y1, x2, y2);

    if (orient == CGAlgorithms.COUNTERCLOCKWISE) {
      return 1;
    } else if (orient == CGAlgorithms.CLOCKWISE) {
      return -1;
    } else {

      // points are collinear - check distance
      final double op = dxp * dxp + dyp * dyp;
      final double oq = dxq * dxq + dyq * dyq;
      if (op < oq) {
        return -1;
      } else if (op > oq) {
        return 1;
      } else {
        return 0;
      }
    }
  }

}
