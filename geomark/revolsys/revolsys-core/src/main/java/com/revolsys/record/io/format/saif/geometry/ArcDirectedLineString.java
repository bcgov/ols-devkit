package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class ArcDirectedLineString extends ArcLineString {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String flowDirection;

  public ArcDirectedLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(geometryFactory, axisCount, vertexCount, coordinates);
  }

  public String getFlowDirection() {
    return this.flowDirection;
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.ARC_DIRECTED;
  }

  public void setFlowDirection(final String flowDirection) {
    this.flowDirection = flowDirection;
  }
}
