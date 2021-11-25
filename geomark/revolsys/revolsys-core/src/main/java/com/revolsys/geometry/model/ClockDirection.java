package com.revolsys.geometry.model;

public enum ClockDirection {
  CLOCKWISE, //
  COUNTER_CLOCKWISE, //
  NONE;

  public static final ClockDirection OGC_SFS_COUNTER_CLOCKWISE = COUNTER_CLOCKWISE;

  public static ClockDirection directionLinePoint(final double x1, final double y1, final double x2,
    final double y2, final double x, final double y) {
    if ((x1 - x) * (y2 - y) - (y1 - y) * (x2 - x) > 0) {
      return COUNTER_CLOCKWISE;
    } else {
      return CLOCKWISE;
    }
  }

  public boolean isClockwise() {
    return this == CLOCKWISE;
  }

  public boolean isCounterClockwise() {
    return this == COUNTER_CLOCKWISE;
  }

  public boolean isNone() {
    return this == COUNTER_CLOCKWISE;
  }

  public ClockDirection opposite() {
    if (this == CLOCKWISE) {
      return COUNTER_CLOCKWISE;
    } else if (this == COUNTER_CLOCKWISE) {
      return CLOCKWISE;
    } else {
      return NONE;
    }
  }
}
