package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxEmpty extends BaseBoundingBox {

  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public BoundingBoxEmpty(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }
}
