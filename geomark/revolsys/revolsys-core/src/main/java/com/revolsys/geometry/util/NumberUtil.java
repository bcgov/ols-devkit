package com.revolsys.geometry.util;

public class NumberUtil {

  public static boolean equalsWithTolerance(final double x1, final double x2,
    final double tolerance) {
    return Math.abs(x1 - x2) <= tolerance;
  }

}
