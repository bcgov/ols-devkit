package com.revolsys.record.io.format.saif.geometry;

import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.MultiPointImpl;
import com.revolsys.record.io.format.saif.SaifConstants;

public class TextOnCurve extends MultiPointImpl {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public TextOnCurve(final GeometryFactory geometryFactory, final List<Point> points) {
    super(geometryFactory, (Point[])points.toArray());
  }

  public String getOsnGeometryType() {
    return SaifConstants.TEXT_ON_CURVE;
  }
}
