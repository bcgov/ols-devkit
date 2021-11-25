package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public abstract class GeometryValidationError extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public GeometryValidationError(final String message) {
    super(message);
  }

  public abstract Geometry getErrorGeometry();

  public abstract Point getErrorPoint();

  public abstract Geometry getGeometry();

}
