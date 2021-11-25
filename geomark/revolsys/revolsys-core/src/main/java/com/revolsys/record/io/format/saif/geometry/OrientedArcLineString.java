package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class OrientedArcLineString extends ArcLineString {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String traversalDirection;

  public OrientedArcLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.ORIENTED_ARC;
  }

  public String getTraversalDirection() {
    return this.traversalDirection;
  }

  public void setTraversalDirection(final String traversalDirection) {
    this.traversalDirection = traversalDirection;
  }
}
