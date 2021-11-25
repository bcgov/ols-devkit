package com.revolsys.record.io.format.saif.geometry;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.record.io.format.saif.SaifConstants;

public class SaifPoint extends PointDoubleGf {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private String qualifier;

  public SaifPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    super(geometryFactory, coordinates);
  }

  public String getOsnGeometryType() {
    return SaifConstants.POINT;
  }

  public String getQualifier() {
    return this.qualifier;
  }

  public void setQualifier(final String qualifier) {
    this.qualifier = qualifier;
  }

}
