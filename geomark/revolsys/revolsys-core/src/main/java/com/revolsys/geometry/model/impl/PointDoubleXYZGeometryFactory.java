package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointDoubleXYZGeometryFactory extends PointDoubleXYZ {
  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public PointDoubleXYZGeometryFactory(final GeometryFactory geometryFactory, final double x,
    final double y, final double z) {
    super(geometryFactory, x, y, z);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Point newPoint(final double x, final double y) {
    return new PointDoubleXYZGeometryFactory(this.geometryFactory, x, y, this.z);
  }
}
