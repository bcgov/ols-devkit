package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.Point;

public class PointDoubleXYOrientation extends PointDoubleXY {

  private static final long serialVersionUID = 1L;

  private final double orientation;

  public PointDoubleXYOrientation(final double x, final double y, final double orientation) {
    super(x, y);
    this.orientation = orientation;
  }

  public PointDoubleXYOrientation(final Point point, final double orientation) {
    super(point);
    this.orientation = orientation;
  }

  public double getOrientation() {
    return this.orientation;
  }
}
