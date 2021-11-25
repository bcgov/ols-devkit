package com.revolsys.geometry.model;

import java.awt.BasicStroke;

public enum LineCap {
  ROUND(BasicStroke.CAP_ROUND, 1), //
  BUTT(BasicStroke.CAP_BUTT, 2), //
  SQUARE(BasicStroke.CAP_SQUARE, 3);

  public static LineCap fromGeometryValue(final int geometryValue) {
    switch (geometryValue) {
      case 1:
        return ROUND;
      case 2:
        return BUTT;
      case 3:
        return SQUARE;
      default:
        throw new IllegalArgumentException("Unknown line cap " + geometryValue);
    }
  }

  private int awtValue;

  private int geometryValue;

  private LineCap(final int awtValue, final int geometryValue) {
    this.awtValue = awtValue;
    this.geometryValue = geometryValue;
  }

  public int getAwtValue() {
    return this.awtValue;
  }

  public int getGeometryValue() {
    return this.geometryValue;
  }
}
