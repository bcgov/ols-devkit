package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public class GeometryError extends AbstractGeometryValidationError {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Geometry errorGeometry;

  public GeometryError(final String message, final Geometry geometry,
    final Geometry errorGeometry) {
    super(message, geometry);
    this.errorGeometry = errorGeometry;
  }

  @Override
  public Geometry getErrorGeometry() {
    return this.errorGeometry;
  }

  @Override
  public Point getErrorPoint() {
    return this.errorGeometry.getPoint();
  }
}
