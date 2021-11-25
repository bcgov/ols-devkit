package com.revolsys.geometry.geoid;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class TriangulatedIrregularNetworkGeoidModel extends AbstractGeoidModel {

  protected TriangulatedIrregularNetwork tin;

  public TriangulatedIrregularNetworkGeoidModel(final String geoidName,
    final TriangulatedIrregularNetwork tin) {
    super(geoidName);
    this.tin = tin;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.tin.getBoundingBox();
  }

  @Override
  public double getGeoidHeight(final double x, final double y) {
    return this.tin.getElevation(x, y);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.tin.getGeometryFactory();
  }
}
