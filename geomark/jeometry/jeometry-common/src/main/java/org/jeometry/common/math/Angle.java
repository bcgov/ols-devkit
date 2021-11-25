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
package org.jeometry.common.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility functions for working with angles.
 * Unless otherwise noted, methods in this class express angles in radians.
 */
public class Angle {

  /** Constant representing clockwise orientation */
  public static final int CLOCKWISE = -1;

  /** Constant representing counterclockwise orientation */
  public static final int COUNTERCLOCKWISE = 1;

  /** Constant representing no orientation */
  public static final int NONE = 0;

  public static final double PI_OVER_2 = Math.PI / 2.0;

  public static final double PI_OVER_4 = Math.PI / 4.0;

  public static final double PI_TIMES_2 = 2.0 * Math.PI;

  /**
   * Calculate the angle of a coordinates
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The distance.
   */
  public static double angle(final double x, final double y) {
    final double angle = Math.atan2(y, x);
    return angle;
  }

  /**
   * Calculate the angle between three coordinates.
   *
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param x3 The third x coordinate.
   * @param y3 The third y coordinate.
   * @return The distance.
   */
  public static double angle(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final double angle1 = angle2d(x2, y2, x1, y1);
    final double angle2 = angle2d(x2, y2, x3, y3);
    return angleDiff(angle1, angle2);
  }

  public static double angle2d(final double x1, final double y1, final double x2, final double y2) {
    return Math.atan2(y2 - y1, x2 - x1);
  }

  public static double angle2dClockwise(final double x1, final double y1, final double x2,
    final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double angle = Math.atan2(dy, dx);
    if (angle == 0) {
      return angle;
    } else if (angle < 0) {
      return -angle;
    } else {
      return PI_TIMES_2 - angle;
    }
  }

  /**
   * Calculate the angle between three coordinates. Uses the SSS theorem http://mathworld.wolfram.com/SSSTheorem.html
   *
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param x3 The third x coordinate.
   * @param y3 The third y coordinate.
   * @return The distance.
   */
  public static double angleBetween(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    final double dxA = x2 - x1;
    final double dyA = y2 - y1;
    final double aSq = dxA * dxA + dyA * dyA;

    final double dxB = x3 - x2;
    final double dyB = y3 - y2;
    final double bSq = dxB * dxB + dyB * dyB;

    final double dxC = x1 - x3;
    final double dyC = y1 - y3;
    final double cSq = dxC * dxC + dyC * dyC;

    final double a = Math.sqrt(aSq);
    final double b = Math.sqrt(bSq);
    final double ab2 = 2 * a * b;
    final double abMinuscSqDivAb2 = (aSq + bSq - cSq) / ab2;
    final double angle = Math.acos(abMinuscSqDivAb2);
    return angle;
  }

  public static double angleBetweenOriented(double angle1, double angle2) {
    if (angle1 < 0) {
      angle1 = PI_TIMES_2 + angle1;
    }
    if (angle2 < 0) {
      angle2 = PI_TIMES_2 + angle2;
    }
    if (angle2 < angle1) {
      angle2 = angle2 + PI_TIMES_2;
    }
    final double angleBetween = angle2 - angle1;
    return angleBetween;
  }

  public static double angleBetweenOriented(final double x1, final double y1, final double x,
    final double y, final double x2, final double y2) {
    final double angle1 = Angle.angle2d(x, y, x1, y1);
    final double angle2 = Angle.angle2d(x, y, x2, y2);

    final double angDelta = angle1 - angle2;

    // normalize, maintaining orientation
    if (angDelta <= -Math.PI) {
      return angDelta + PI_TIMES_2;
    }
    if (angDelta > Math.PI) {
      return angDelta - PI_TIMES_2;
    }
    return angDelta;
  }

  public static double angleDegreeDiff(final double from, final double to) {
    return to - from;
  }

  public static double angleDegreeDiffOriented(final double from, final double to) {
    double diff = to - from;
    if (diff < 0) {
      diff += 360;
    }
    if (diff < 180) {
      diff = 360 - diff;
    }
    return diff;
  }

  public static double angleDegreeNorthClockwiseFromRadian(final double rad) {
    final double degrees = Math.toDegrees(rad);
    return getNorthClockwiseAngle(degrees);
  }

  public static double angleDegrees(final double x1, final double y1, final double x2,
    final double y2) {
    final double width = x2 - x1;
    final double height = y2 - y1;
    if (width == 0) {
      if (height < 0) {
        return 270;
      } else {
        return 90;
      }
    } else if (height == 0) {
      if (width < 0) {
        return 180;
      } else {
        return 0;
      }
    }
    final double arctan = Math.atan(height / width);
    double degrees = Math.toDegrees(arctan);
    if (width < 0) {
      degrees = 180 + degrees;
    } else {
      degrees = (360 + degrees) % 360;
    }
    return degrees;
  }

  /**
   * Computes the unoriented smallest difference between two angles.
   * The angles are assumed to be normalized to the range [-Pi, Pi].
   * The result will be in the range [0, Pi].
   *
   * @param angle1 the angle of one vector (in [-Pi, Pi] )
   * @param angle2 the angle of the other vector (in range [-Pi, Pi] )
   * @return the angle (in radians) between the two vectors (in range [0, Pi] )
   */
  public static double angleDiff(final double angle1, final double angle2) {
    double delAngle;

    if (angle1 < angle2) {
      delAngle = angle2 - angle1;
    } else {
      delAngle = angle1 - angle2;
    }

    if (delAngle > Math.PI) {
      delAngle = 2 * Math.PI - delAngle;
    }

    return delAngle;
  }

  public static double angleDiff(final double angle1, final double angle2,
    final boolean clockwise) {
    if (clockwise) {
      if (angle2 < angle1) {
        final double angle = angle2 + Math.PI * 2 - angle1;
        return angle;
      } else {
        final double angle = angle2 - angle1;
        return angle;
      }
    } else {
      if (angle1 < angle2) {
        final double angle = angle1 + Math.PI * 2 - angle2;
        return angle;
      } else {
        final double angle = angle1 - angle2;
        return angle;
      }
    }
  }

  public static double angleDiffDegrees(final double a, final double b) {
    final double largest = Math.max(a, b);
    final double smallest = Math.min(a, b);
    double diff = largest - smallest;
    if (diff > 180) {
      diff = 360 - diff;
    }
    return diff;
  }

  public static double angleNorthDegrees(final double x1, final double y1, final double x2,
    final double y2) {
    final double angle = angleDegrees(x1, y1, x2, y2);
    return getNorthClockwiseAngle(angle);
  }

  public static double asinh(double a) {
    boolean negative = false;
    if (a < 0) {
      negative = true;
      a = -a;
    }

    final double absAsinh = Math.log(a + Math.sqrt(Math.pow(a, 2) + 1));
    return negative ? -absAsinh : absAsinh;
  }

  public static double atanh(double a) {
    boolean negative = false;
    if (a < 0) {
      negative = true;
      a = -a;
    }

    final double absAtanh = 0.5 * Math.log((1 + a) / (1 - a));

    return negative ? -absAtanh : absAtanh;
  }

  public static double getNorthClockwiseAngle(final double angle) {
    final double northAngle = (450 - angle) % 360;
    return northAngle;
  }

  /**
   * Returns whether an angle must turn clockwise or counterclockwise
   * to overlap another angle.
   *
   * @param ang1 an angle (in radians)
   * @param ang2 an angle (in radians)
   * @return whether a1 must turn CLOCKWISE, COUNTERCLOCKWISE or NONE to
   * overlap a2.
   */
  public static int getTurn(final double ang1, final double ang2) {
    final double crossproduct = Math.sin(ang2 - ang1);

    if (crossproduct > 0) {
      return COUNTERCLOCKWISE;
    }
    if (crossproduct < 0) {
      return CLOCKWISE;
    }
    return NONE;
  }

  public static boolean isAcute(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final double dx0 = x1 - x2;
    final double dy0 = y1 - y2;
    final double dx1 = x3 - x2;
    final double dy1 = y3 - y2;
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  /**
   * Computes the normalized value of an angle, which is the
   * equivalent angle in the range ( -Pi, Pi ].
   *
   * @param angle the angle to normalize
   * @return an equivalent angle in the range (-Pi, Pi]
   */
  public static double normalize(double angle) {
    while (angle > Math.PI) {
      angle -= PI_TIMES_2;
    }
    while (angle <= -Math.PI) {
      angle += PI_TIMES_2;
    }
    return angle;
  }

  /**
   * Computes the normalized positive value of an angle, which is the
   * equivalent angle in the range [ 0, 2*Pi ).
   * E.g.:
   * <ul>
   * <li>normalizePositive(0.0) = 0.0
   * <li>normalizePositive(-PI) = PI
   * <li>normalizePositive(-2PI) = 0.0
   * <li>normalizePositive(-3PI) = PI
   * <li>normalizePositive(-4PI) = 0
   * <li>normalizePositive(PI) = PI
   * <li>normalizePositive(2PI) = 0.0
   * <li>normalizePositive(3PI) = PI
   * <li>normalizePositive(4PI) = 0.0
   * </ul>
   *
   * @param angle the angle to normalize, in radians
   * @return an equivalent positive angle
   */
  public static double normalizePositive(double angle) {
    if (angle < 0.0) {
      while (angle < 0.0) {
        angle += PI_TIMES_2;
      }
      // in case round-off error bumps the value over
      if (angle >= PI_TIMES_2) {
        angle = 0.0;
      }
    } else {
      while (angle >= PI_TIMES_2) {
        angle -= PI_TIMES_2;
      }
      // in case round-off error bumps the value under
      if (angle < 0.0) {
        angle = 0.0;
      }
    }
    return angle;
  }

  public static double toDecimalDegrees(double degrees, final double minutes,
    final double seconds) {
    boolean negative = false;
    if (degrees < 0) {
      degrees = -degrees;
      negative = true;
    }
    final double decimalDegrees = degrees + minutes / 60.0 + seconds / 3600;
    if (negative) {
      return -decimalDegrees;
    } else {
      return decimalDegrees;
    }
  }

  public static double toDecimalDegrees(String text) {
    if (text != null) {
      text = text.toString().trim();

      if (text.length() > 0) {
        boolean negative = false;
        if (text.endsWith("S") || text.endsWith("W")) {
          negative = true;
          text = text.substring(0, text.length() - 1).trim();
        } else if (text.endsWith("E") || text.endsWith("N")) {
          text = text.substring(0, text.length() - 1).trim();
        }
        final String[] parts = text.split("[\\*°'\":\\s]+");
        double decimalDegrees = 0;
        if (parts.length > 0) {
          decimalDegrees = Double.parseDouble(parts[0]);
          System.out.println(decimalDegrees);
          if (decimalDegrees < 0) {
            negative = true;
            decimalDegrees = -decimalDegrees;
          }
        }
        if (parts.length > 1) {
          final double minutes = Double.parseDouble(parts[1]) / 60;
          System.out
            .println(new BigDecimal(minutes).setScale(20, RoundingMode.HALF_UP).toPlainString());
          if (!Double.isNaN(minutes)) {
            decimalDegrees += minutes;
          }
        }
        if (parts.length > 2) {
          final double seconds = Double.parseDouble(parts[2]) / 3600;
          System.out
            .println(new BigDecimal(seconds).setScale(20, RoundingMode.HALF_UP).toPlainString());
          if (!Double.isNaN(seconds)) {
            decimalDegrees += seconds;
          }
        }
        if (negative) {
          return -decimalDegrees;
        } else {
          return decimalDegrees;
        }
      }
    }
    return Double.NaN;
  }

  public static String toDegreesMinutesSeconds(final double decimalDegrees) {
    if (!Double.isNaN(decimalDegrees)) {
      final double degrees = Math.floor(decimalDegrees);
      final StringBuilder text = new StringBuilder();
      text.append(degrees);
      text.append('°');
      final double decimal = decimalDegrees - degrees;
      final double minutes = Math.floor(decimal * 60);
      if (minutes < 10) {
        text.append('0');
      }
      text.append(minutes);
      text.append('\'');
      final double seconds = decimal * 3600 % 60;
      if (seconds < 10) {
        text.append('0');
      }
      text.append(seconds);
      text.append('\'');
      return text.toString();
    }
    return null;
  }

  public static String toDegreesMinutesSeconds(double decimalDegrees,
    final int secondsDecimalPlaces, final boolean spaceSeparator) {
    if (!Double.isNaN(decimalDegrees)) {
      final StringBuilder text = new StringBuilder();
      if (decimalDegrees < 0) {
        text.append('-');
        decimalDegrees = -decimalDegrees;
      }
      final int degrees = (int)Math.floor(decimalDegrees);
      if (degrees < 10) {
        text.append('0');
      }
      text.append(degrees);
      if (spaceSeparator) {
        text.append(' ');
      } else {
        text.append('°');
      }
      final double decimal = decimalDegrees - degrees;
      final int minutes = (int)Math.floor(decimal * 60);
      if (minutes < 10) {
        text.append('0');
      }
      text.append(minutes);
      if (spaceSeparator) {
        text.append(' ');
      } else {
        text.append('\'');
      }
      final double seconds = decimalDegrees * 3600 % 60;
      final BigDecimal secondsBig = new BigDecimal(seconds);
      if (seconds < 10) {
        text.append('0');
      }
      text.append(secondsBig.setScale(secondsDecimalPlaces, RoundingMode.HALF_UP).toPlainString());
      if (!spaceSeparator) {
        text.append('\'');
      }
      return text.toString();
    }
    return null;
  }

  public static String toDegreesMinutesSecondsLat(double angle, final int secondsDecimalPlaces) {
    if (!Double.isNaN(angle)) {
      boolean negative = false;
      if (angle < 0) {
        angle = -angle;
        negative = true;
      }
      final int degrees = (int)angle;
      final double f = (angle - degrees) * 60;
      int minutes = (int)f;
      double seconds = (f - minutes) * 60;
      if (Math.abs(seconds - 60) <= 1e-6) {
        seconds = 0.;
        ++minutes;
      }

      final StringBuilder text = new StringBuilder();
      if (degrees < 10) {
        text.append('0');
      }
      text.append(degrees);
      text.append(' ');
      if (minutes < 10) {
        text.append('0');
      }
      text.append(minutes);
      text.append(' ');
      final BigDecimal secondsBig = new BigDecimal(seconds);
      if (seconds < 10) {
        text.append('0');
      }
      text.append(secondsBig.setScale(secondsDecimalPlaces, RoundingMode.HALF_UP).toPlainString());

      if (negative) {
        text.append('S');
      } else {
        text.append('N');
      }
      return text.toString();
    }
    return null;
  }

  public static String toDegreesMinutesSecondsLon(double angle, final int secondsDecimalPlaces) {
    if (!Double.isNaN(angle)) {
      boolean negative = false;
      if (angle < 0) {
        angle = -angle;
        negative = true;
      }
      final int degrees = (int)angle;
      final double f = (angle - degrees) * 60;
      int minutes = (int)f;
      double seconds = (f - minutes) * 60;
      if (Math.abs(seconds - 60) <= 1e-6) {
        seconds = 0.;
        ++minutes;
      }

      final StringBuilder text = new StringBuilder();
      if (degrees < 100) {
        text.append('0');
      }
      if (degrees < 10) {
        text.append('0');
      }
      text.append(degrees);
      text.append(' ');
      if (minutes < 10) {
        text.append('0');
      }
      text.append(minutes);
      text.append(' ');
      final BigDecimal secondsBig = new BigDecimal(seconds);
      if (seconds < 10) {
        text.append('0');
      }
      text.append(secondsBig.setScale(secondsDecimalPlaces, RoundingMode.HALF_UP).toPlainString());

      if (negative) {
        text.append('W');
      } else {
        text.append('E');
      }
      return text.toString();
    }
    return null;
  }

  public static String toDmsString(final double angle) {
    return (int)Math.floor(angle) + " " + (int)Math.floor(angle * 60 % 60) + " "
      + angle * 3600 % 60;
  }

  public static String toDmsStringRadians(double angle) {
    angle = Math.toDegrees(angle);
    return toDmsString(angle);
  }
}
