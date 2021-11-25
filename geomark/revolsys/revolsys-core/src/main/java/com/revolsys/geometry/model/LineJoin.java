package com.revolsys.geometry.model;

import java.awt.BasicStroke;

public enum LineJoin {
  ROUND(BasicStroke.JOIN_ROUND, 1), //
  MITER(BasicStroke.JOIN_MITER, 2), //
  BEVEL(BasicStroke.JOIN_BEVEL, 3);

  public static LineJoin fromGeometryValue(final int geometryValue) {
    switch (geometryValue) {
      case 1:
        return ROUND;
      case 2:
        return MITER;
      case 3:
        return BEVEL;
      default:
        throw new IllegalArgumentException("Unknown line join " + geometryValue);
    }
  }

  private int awtValue;

  private int geometryValue;

  private LineJoin(final int awtValue, final int geometryValue) {
    this.awtValue = awtValue;
    this.geometryValue = geometryValue;
  }

  public int getAwtValue() {
    return this.awtValue;
  }

  public int getGeometryValue() {
    return this.geometryValue;
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
