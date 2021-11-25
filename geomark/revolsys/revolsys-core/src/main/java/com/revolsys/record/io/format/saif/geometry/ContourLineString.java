package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class ContourLineString extends ArcLineString {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String form;

  private int value;

  public ContourLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(geometryFactory, axisCount, vertexCount, coordinates);
  }

  public String getForm() {
    return this.form;
  }

  @Override
  public String getOsnGeometryType() {
    return SaifConstants.CONTOUR;
  }

  public int getValue() {
    return this.value;
  }

  public void setForm(final String form) {
    this.form = form;
  }

  public void setValue(final int value) {
    this.value = value;
  }
}
