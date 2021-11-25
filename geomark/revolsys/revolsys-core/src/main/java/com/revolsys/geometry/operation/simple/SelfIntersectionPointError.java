package com.revolsys.geometry.operation.simple;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.valid.GeometryError;

public class SelfIntersectionPointError extends GeometryError {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public SelfIntersectionPointError(final Geometry geometry, final Point point) {
    super("Self Intersection at Point", geometry, point);
  }
}
