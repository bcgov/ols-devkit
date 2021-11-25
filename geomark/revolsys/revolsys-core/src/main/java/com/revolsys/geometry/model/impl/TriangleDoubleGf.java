package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;

public class TriangleDoubleGf extends TriangleDouble {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public TriangleDoubleGf(final GeometryFactory geometryFactory, final double... coordinates) {
    super(coordinates);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

}
