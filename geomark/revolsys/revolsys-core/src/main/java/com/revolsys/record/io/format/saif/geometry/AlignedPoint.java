package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class AlignedPoint extends SaifPoint {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private double alignment;

  private String directionIndicator;

  private String north;

  public AlignedPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    super(geometryFactory, coordinates);
  }

  public double getAlignment() {
    return this.alignment;
  }

  public String getDirectionIndicator() {
    return this.directionIndicator;
  }

  public String getNorth() {
    return this.north;
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.ALIGNED_POINT;
  }

  public void setAlignment(final double alignment) {
    this.alignment = alignment;
  }

  public void setDirectionIndicator(final String directionIndicator) {
    this.directionIndicator = directionIndicator;
  }

  public void setNorth(final String north) {
    this.north = north;
  }

}
