package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public abstract class AbstractGeometryValidationError extends GeometryValidationError {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Geometry geometry;

  public AbstractGeometryValidationError(final String message, final Geometry geometry) {
    super(message);
    this.geometry = geometry;
  }

  @Override
  public Geometry getErrorGeometry() {
    return getErrorPoint();
  }

  @Override
  public Point getErrorPoint() {
    return this.geometry.getPoint();
  }

  @Override
  public Geometry getGeometry() {
    return this.geometry;
  }
}
