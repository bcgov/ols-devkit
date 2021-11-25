package com.revolsys.geometry.util;

import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Consumer6Double;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class RectangleUtil {
  // https://en.wikipedia.org/wiki/Cohen–Sutherland_algorithm
  public static void clipLine(final double minX, final double minY, final double maxX,
    final double maxY, double lineX1, double lineY1, double lineX2, double lineY2,
    final Consumer4Double action) {
    // compute outcodes for P0, P1, and whatever point lies outside the clip
    // rectangle
    int outcode1 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX1, lineY1);
    int outcode2 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX2, lineY2);
    boolean accept = false;

    while (!accept) {
      if ((outcode1 | outcode2) == 0) {
        // Bitwise OR is 0. Trivially accept and get out of loop
        accept = true;
      } else if ((outcode1 & outcode2) != 0) {
        // Bitwise AND is not 0. (implies both end points are in the same region
        // outside the
        // window). Reject and get out of loop
        return;
      } else {
        // failed both tests, so calculate the line segment to clip
        // from an outside point to an intersection with clip edge
        double x;
        double y;

        // At least one endpoint is outside the clip rectangle; pick it.
        final int outcodeOut = outcode1 != 0 ? outcode1 : outcode2;

        // Now find the intersection point;
        // use formulas:
        // slope = (y1 - y0) / (x1 - x0)
        // x = x0 + (1 / slope) * (ym - y0), where ym is ymin or maxY
        // y = y0 + slope * (xm - x0), where xm is xmin or xmax
        final double deltaY = lineY2 - lineY1;
        final double deltaX = lineX2 - lineX1;
        if (OutCode.isTop(outcodeOut)) { // point is above the clip rectangle
          final double ratio = (maxY - lineY1) / deltaY;
          x = lineX1 + deltaX * ratio;
          y = maxY;
        } else if (OutCode.isBottom(outcodeOut)) { // point is below the clip
                                                   // rectangle
          final double ratio = (minY - lineY1) / deltaY;
          x = lineX2 + deltaX * ratio;
          y = minY;
        } else if (OutCode.isRight(outcodeOut)) { // point is to the right of
                                                  // clip
          final double ratio = (maxX - lineX1) / deltaX;
          x = maxX;
          y = lineY1 + deltaY * ratio;
        } else if (OutCode.isLeft(outcodeOut)) { // point is to the left of clip
                                                 // rectangle
          final double ratio = (minX - lineX1) / deltaX;
          x = minX;
          y = lineY1 + deltaY * ratio;
        } else {
          throw new IllegalStateException("Cannot clip as both points are inside the rectangle");
        }

        // Now we move outside point to intersection point to clip
        // and get ready for next pass.
        if (outcodeOut == outcode1) {
          lineX1 = x;
          lineY1 = y;
          outcode1 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX1, lineY1);
        } else {
          lineX2 = x;
          lineY2 = y;
          outcode2 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX2, lineY2);
        }
      }
    }
    if (accept) {
      action.accept(lineX2, lineY2, lineX2, lineY2);
    }
  }

  // https://en.wikipedia.org/wiki/Cohen–Sutherland_algorithm
  public static void clipLine(final double minX, final double minY, final double maxX,
    final double maxY, double lineX1, double lineY1, double lineZ1, double lineX2, double lineY2,
    double lineZ2, final Consumer6Double action) {
    // compute outcodes for P0, P1, and whatever point lies outside the clip
    // rectangle
    int outcode1 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX1, lineY1);
    int outcode2 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX2, lineY2);
    boolean accept = false;

    while (!accept) {
      if ((outcode1 | outcode2) == 0) {
        // Bitwise OR is 0. Trivially accept and get out of loop
        accept = true;
      } else if ((outcode1 & outcode2) != 0) {
        // Bitwise AND is not 0. (implies both end points are in the same region
        // outside the
        // window). Reject and get out of loop
        return;
      } else {
        // failed both tests, so calculate the line segment to clip
        // from an outside point to an intersection with clip edge
        double x;
        double y;
        double z;

        // At least one endpoint is outside the clip rectangle; pick it.
        final int outcodeOut = outcode1 != 0 ? outcode1 : outcode2;

        // Now find the intersection point;
        // use formulas:
        // slope = (y1 - y0) / (x1 - x0)
        // x = x0 + (1 / slope) * (ym - y0), where ym is ymin or maxY
        // y = y0 + slope * (xm - x0), where xm is xmin or xmax
        final double deltaY = lineY2 - lineY1;
        final double deltaX = lineX2 - lineX1;
        if (OutCode.isTop(outcodeOut)) { // point is above the clip rectangle
          final double ratio = (maxY - lineY1) / deltaY;
          x = lineX1 + deltaX * ratio;
          y = maxY;
          z = lineZ1 + deltaX * ratio;
        } else if (OutCode.isBottom(outcodeOut)) { // point is below the clip
                                                   // rectangle
          final double ratio = (minY - lineY1) / deltaY;
          x = lineX2 + deltaX * ratio;
          y = minY;
          z = lineZ1 + deltaX * ratio;
        } else if (OutCode.isRight(outcodeOut)) { // point is to the right of
                                                  // clip
                                                  // rectangle
          final double ratio = (maxX - lineX1) / deltaX;
          y = lineY1 + deltaY * ratio;
          x = maxX;
          z = lineZ1 + deltaY * ratio;
        } else if (OutCode.isLeft(outcodeOut)) { // point is to the left of clip
                                                 // rectangle
          final double ratio = (minX - lineX1) / deltaX;
          y = lineY1 + deltaY * ratio;
          x = minX;
          z = lineZ1 + deltaY * ratio;
        } else {
          throw new IllegalStateException("Cannot clip as both points are inside the rectangle");
        }

        // Now we move outside point to intersection point to clip
        // and get ready for next pass.
        if (outcodeOut == outcode1) {
          lineX1 = x;
          lineY1 = y;
          lineZ1 = z;
          outcode1 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX1, lineY1);
        } else {
          lineX2 = x;
          lineY2 = y;
          lineZ2 = z;
          outcode2 = OutCode.getOutcode(minX, minY, maxX, maxY, lineX2, lineY2);
        }
      }
    }
    if (accept) {
      action.accept(lineX2, lineY2, lineZ1, lineX2, lineY2, lineZ2);
    }
  }

  public static boolean covers(final double minX1, final double minY1, final double maxX1,
    final double maxY1, final double minX2, final double minY2, final double maxX2,
    final double maxY2) {
    return minX1 <= minX2 && maxX2 <= maxX1 && minY1 <= minY2 && maxY2 <= maxY1;
  }

  public static boolean coversPointMinMax(final double minX, final double minY, final double maxX,
    final double maxY, final double x, final double y) {
    return intersectsPointMinMax(minX, maxX, minY, maxY, x, y);
  }

  public static void expand(final double[] bounds, final int axisCount,
    final BoundingBox boundingBox) {
    if (boundingBox != null) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double min = boundingBox.getMin(axisIndex);
        expand(bounds, axisCount, axisIndex, min);

        final double max = boundingBox.getMax(axisIndex);
        expand(bounds, axisCount, axisIndex, max);
      }
    }
  }

  public static void expand(final double[] bounds, final int axisCount,
    final double... coordinates) {
    for (int axisIndex = 0; axisIndex < axisCount && axisIndex < coordinates.length; axisIndex++) {
      final double coordinate = coordinates[axisIndex];
      expand(bounds, axisCount, axisIndex, coordinate);
    }
  }

  public static void expand(final double[] bounds, final int axisCount, final Geometry geometry) {
    if (geometry != null) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      expand(bounds, axisCount, boundingBox);
    }
  }

  public static void expand(final double[] bounds, final int axisCount, final int axisIndex,
    final double coordinate) {
    if (Double.isFinite(coordinate)) {
      final double min = bounds[axisIndex];
      if (coordinate < min || !Double.isFinite(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || !Double.isFinite(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final double[] bounds, final int axisCount, final Point point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = point.getCoordinate(axisIndex);
      expand(bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final double... values) {
    final int axisCount = bounds.length / 2;
    for (int i = 0; i < values.length; i++) {
      final double value = values[i];
      final int axisIndex = i % axisCount;
      expand(geometryFactory, bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final int axisIndex, double coordinate) {
    if (geometryFactory != null) {
      coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
    }
    if (Double.isFinite(coordinate)) {
      final int axisCount = bounds.length / 2;
      final double min = bounds[axisIndex];
      if (coordinate < min || !Double.isFinite(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || !Double.isFinite(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final int axisCount, final int axisIndex, double coordinate) {
    if (geometryFactory != null) {
      coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
    }
    if (Double.isFinite(coordinate)) {
      final double min = bounds[axisIndex];
      if (coordinate < min || !Double.isFinite(min)) {
        bounds[axisIndex] = coordinate;
      }
      final double max = bounds[axisCount + axisIndex];
      if (coordinate > max || !Double.isFinite(max)) {
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
  }

  public static void expand(final GeometryFactory geometryFactory, final double[] bounds,
    Point point) {
    final int axisCount = bounds.length / 2;
    point = point.convertGeometry(geometryFactory, axisCount);
    final int count = Math.min(axisCount, point.getAxisCount());
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      if (Double.isFinite(coordinate)) {
        expand(geometryFactory, bounds, axisCount, axisIndex, coordinate);
      }
    }
  }

  public static void expandX(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 0, value);
  }

  public static void expandY(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 1, value);
  }

  public static void expandZ(final double[] bounds, final int axisCount, final double value) {
    expand(bounds, axisCount, 2, value);
  }

  public static double getMax(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NEGATIVE_INFINITY;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > axisCount) {
        return Double.NEGATIVE_INFINITY;
      } else {
        final double max = bounds[axisCount + axisIndex];
        return max;
      }
    }
  }

  public static double getMin(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.POSITIVE_INFINITY;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex >= axisCount) {
        return Double.POSITIVE_INFINITY;
      } else {
        final double min = bounds[axisIndex];
        return min;
      }
    }
  }

  public static boolean intersects(final double minX1, final double minY1, final double maxX1,
    final double maxY1, double x1, double y1, double x2, double y2) {
    if (x1 > x2) {
      final double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      final double t = y1;
      y1 = y2;
      y2 = t;
    }
    return !(x1 > maxX1 || x2 < minX1 || y1 > maxY1 || y2 < minY1);
  }

  public static boolean intersects(final double[] bounds1, final double[] bounds2) {
    if (bounds1 == null || bounds2 == null) {
      return false;
    } else {
      final int axisCount1 = bounds1.length / 2;
      final double minX1 = bounds1[0];
      final double minY1 = bounds1[1];
      final double maxX1 = bounds1[axisCount1];
      final double maxY1 = bounds1[axisCount1 + 1];

      final int axisCount2 = bounds2.length / 2;
      final double minX2 = bounds2[0];
      final double minY2 = bounds2[1];
      final double maxX2 = bounds2[axisCount2];
      final double maxY2 = bounds2[axisCount2 + 1];

      return !(minX2 > maxX1 || maxX2 < minX1 || minY2 > maxY1 || maxY2 < minY1);
    }
  }

  /**
   * Point intersects the bounding box of the line.
   *
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final Point lineStart, final Point lineEnd, final Point point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();
    return intersectsPoint(x1, y1, x2, y2, x, y);
  }

  /**
   * Tests whether the envelope defined by p1-p2
   * and the envelope defined by q1-q2
   * intersect.
   *
   * @param p1 one extremal point of the envelope P
   * @param p2 another extremal point of the envelope P
   * @param q1 one extremal point of the envelope Q
   * @param q2 another extremal point of the envelope Q
   * @return <code>true</code> if Q intersects P
   */
  public static boolean intersects(final Point line1Start, final Point line1End,
    final Point line2Start, final Point line2End) {
    final double line1x1 = line1Start.getX();
    final double line1y1 = line1Start.getY();
    final double line1x2 = line1End.getX();
    final double line1y2 = line1End.getY();

    final double line2x1 = line2Start.getX();
    final double line2y1 = line2Start.getY();
    final double line2x2 = line2End.getX();
    final double line2y2 = line2End.getY();
    return intersectsMinMax(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2, line2y2);
  }

  public static boolean intersectsLine(final double minX, final double minY, final double maxX,
    final double maxY, double x1, double y1, final double x2, final double y2) {
    final int out2 = OutCode.getOutcode(minX, minY, maxX, maxY, x2, y2);
    if (out2 == 0) {
      return true;
    }
    int out1 = OutCode.getOutcode(minX, minY, maxX, maxY, x1, y1);
    while (out1 != 0) {
      if ((out1 & out2) != 0) {
        return false;
      } else if ((out1 & (OutCode.OUT_LEFT | OutCode.OUT_RIGHT)) != 0) {
        double x;
        if ((out1 & OutCode.OUT_RIGHT) != 0) {
          x = maxX;
          out1 &= ~OutCode.OUT_RIGHT;
        } else {
          x = minX;
          out1 &= ~OutCode.OUT_LEFT;
        }
        y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
        x1 = x;
      } else {
        double y;
        if ((out1 & OutCode.OUT_TOP) != 0) {
          y = maxY;
          out1 &= ~OutCode.OUT_TOP;
        } else {
          y = minY;
          out1 &= ~OutCode.OUT_BOTTOM;
        }
        x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
        y1 = y;
      }
    }
    return true;
  }

  public static boolean intersectsMinMax(final double p1X, final double p1Y, final double p2X,
    final double p2Y, final double q1X, final double q1Y, final double q2X, final double q2Y) {
    double minPX;
    double maxPX;
    if (p1X < p2X) {
      minPX = p1X;
      maxPX = p2X;
    } else {
      minPX = p2X;
      maxPX = p1X;
    }
    double minQX;
    double maxQX;
    if (q1X < q2X) {
      minQX = q1X;
      maxQX = q2X;
    } else {
      minQX = q2X;
      maxQX = q1X;
    }

    if (minPX > maxQX) {
      return false;
    } else {
      if (maxPX < minQX) {
        return false;
      } else {
        double minPY;
        double maxPY;
        if (p1Y < p2Y) {
          minPY = p1Y;
          maxPY = p2Y;
        } else {
          minPY = p2Y;
          maxPY = p1Y;
        }
        double minQY;
        double maxQY;
        if (q1Y < q2Y) {
          minQY = q1Y;
          maxQY = q2Y;
        } else {
          minQY = q2Y;
          maxQY = q1Y;
        }

        if (minPY > maxQY) {
          return false;
        } else {
          if (maxPY < minQY) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  public static boolean intersectsOutcode(final double minX1, final double minY1,
    final double maxX1, final double maxY1, double minX2, double minY2, double maxX2,
    double maxY2) {
    int out1 = OutCode.getOutcode(minX1, minY1, maxX1, maxX2, minX2, minY2);
    int out2 = OutCode.getOutcode(minX1, minY1, maxX1, maxX2, maxX2, maxY2);
    while (true) {
      if ((out1 | out2) == 0) {
        return true;
      } else if ((out1 & out2) != 0) {
        return false;
      } else {

        int out;
        if (out1 != 0) {
          out = out1;
        } else {
          out = out2;
        }

        double x = 0;
        double y = 0;
        if (OutCode.isTop(out)) {
          x = minX2 + (maxX2 - minX2) * (maxY1 - minY2) / (maxY2 - minY2);
          y = maxY1;
        } else if (OutCode.isBottom(out)) {
          x = minX2 + (maxX2 - minX2) * (minY1 - minY2) / (maxY2 - minY2);
          y = minY1;
        } else if (OutCode.isRight(out)) {
          y = minY2 + (maxY2 - minY2) * (maxX1 - minX2) / (maxX2 - minX2);
          x = maxX1;
        } else if (OutCode.isLeft(out)) {
          y = minY2 + (maxY2 - minY2) * (minX1 - minX2) / (maxX2 - minX2);
          x = minX1;
        }

        if (out == out1) {
          minX2 = x;
          minY2 = y;
          out1 = OutCode.getOutcode(minX1, minY1, maxX1, maxX2, minX2, minY2);
        } else {
          maxX2 = x;
          maxY2 = y;
          out2 = OutCode.getOutcode(minX1, minY1, maxX1, maxX2, maxX2, maxY2);
        }
      }
    }
  }

  /**
   * Point intersects the bounding box of the line.
   *
   */
  public static boolean intersectsPoint(final double x1, final double y1, final double x2,
    final double y2, final double x, final double y) {
    double minX;
    double maxX;
    if (x1 <= x2) {
      minX = x1;
      maxX = x2;
    } else {
      minX = x2;
      maxX = x1;
    }
    double minY;
    double maxY;
    if (y1 <= y2) {
      minY = y1;
      maxY = y2;
    } else {
      minY = y2;
      maxY = y1;
    }
    return intersectsPointMinMax(minX, maxX, minY, maxY, x, y);
  }

  public static boolean intersectsPointMinMax(final double minX, final double maxX,
    final double minY, final double maxY, final double x, final double y) {
    return x >= minX && x <= maxX && y >= minY && y <= maxY;
  }

  public static boolean isEmpty(final BoundingBox boundingBox) {
    if (boundingBox == null) {
      return true;
    } else {
      return boundingBox.isEmpty();
    }
  }

  public static double[] newBounds(final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = newBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = bounds[axisIndex];
      if (Double.isFinite(coordinate)) {
        newBounds[axisIndex] = coordinate;
        newBounds[axisCount + axisCount] = coordinate;
      }
    }
    return newBounds;
  }

  public static double[] newBounds(final GeometryFactory geometryFactory, final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = newBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double coordinate = bounds[axisIndex];
      if (geometryFactory != null) {
        coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
      }
      if (Double.isFinite(coordinate)) {
        newBounds[axisIndex] = coordinate;
        newBounds[axisCount + axisIndex] = coordinate;
      }
    }
    return newBounds;
  }

  public static double[] newBounds(final GeometryFactory geometryFactory, final int axisCount,
    Point point) {
    point = point.convertGeometry(geometryFactory, axisCount);
    final double[] bounds = newBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double coordinate = point.getCoordinate(axisIndex);
      if (geometryFactory != null) {
        coordinate = geometryFactory.makePrecise(axisIndex, coordinate);
      }
      if (Double.isFinite(coordinate)) {
        bounds[axisIndex] = coordinate;
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
    return bounds;
  }

  public static double[] newBounds(final GeometryFactory geometryFactory, final Point point) {
    final int axisCount = point.getAxisCount();
    return newBounds(geometryFactory, axisCount, point);
  }

  public static double[] newBounds(final int axisCount) {
    final double[] newBounds = new double[axisCount * 2];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      newBounds[axisIndex] = Double.POSITIVE_INFINITY;
      newBounds[axisCount + axisIndex] = Double.NEGATIVE_INFINITY;
    }
    return newBounds;
  }

  public static double[] newBounds(final int axisCount, final Point point) {
    final double[] bounds = newBounds(axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      if (Double.isFinite(coordinate)) {
        bounds[axisIndex] = coordinate;
        bounds[axisCount + axisIndex] = coordinate;
      }
    }
    return bounds;
  }

  public static double[] newBounds(final Point point) {
    final int axisCount = point.getAxisCount();
    return newBounds(axisCount, point);
  }

  public static double[] newBounds2d() {
    return new double[] {
      Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
      Double.NEGATIVE_INFINITY
    };
  }
}
