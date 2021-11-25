package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxDoubleXYGeometryFactory extends BoundingBoxDoubleXY {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public BoundingBoxDoubleXYGeometryFactory(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    super(x1, y1, x2, y2);
    this.geometryFactory = geometryFactory;
    makePrecise();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  private void makePrecise() {
    this.minX = this.geometryFactory.makeXPreciseFloor(this.minX);
    this.maxX = this.geometryFactory.makeXPreciseCeil(this.maxX);
    this.minY = this.geometryFactory.makeYPreciseFloor(this.minY);
    this.maxY = this.geometryFactory.makeYPreciseCeil(this.maxY);
  }

  @Override
  public RectangleXY toRectangle() {
    return this.geometryFactory.newRectangleCorners(this.minX, this.minY, this.maxX, this.maxY);
  }
}
