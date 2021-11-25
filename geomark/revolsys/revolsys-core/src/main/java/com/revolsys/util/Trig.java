package com.revolsys.util;

public class Trig {

  private static double adjacent(final double angle, final double length) {
    return Math.cos(angle) * length;
  }

  public static double adjacent(final double value, final double angle, final double length) {
    return value + adjacent(angle, length);
  }

  private static double opposite(final double angle, final double length) {
    return Math.sin(angle) * length;
  }

  public static double opposite(final double value, final double angle, final double length) {
    return value + opposite(angle, length);
  }

}
