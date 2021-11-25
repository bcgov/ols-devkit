package com.revolsys.record.io.format.saif.util;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

public class OsnConverterRegistry {
  private final Map<String, OsnConverter> converters = new HashMap<>();

  private GeometryFactory geometryFactory;

  public OsnConverterRegistry() {
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(1.0, 1.0, 1.0);
    init(geometryFactory);
  }

  public OsnConverterRegistry(final int srid) {
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(srid, 1.0, 1.0, 1.0);

    init(geometryFactory);
  }

  private void addConverter(final String name, final OsnConverter converter) {
    this.converters.put(name, converter);
  }

  public OsnConverter getConverter(String name) {
    if (name == null) {
      return null;
    } else {
      if (name.startsWith("/")) {
        name = name.substring(1);
      }
      return this.converters.get(name);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public void init(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    addConverter("Date", new DateConverter());
    addConverter("SpatialObject", new SpatialObjectConverter(this));
    addConverter(SaifConstants.ARC, new ArcConverter(geometryFactory));
    addConverter(SaifConstants.ORIENTED_ARC, new OrientedArcConverter(geometryFactory, this));
    addConverter(SaifConstants.ARC_DIRECTED, new ArcDirectedConverter(geometryFactory));
    addConverter(SaifConstants.CONTOUR, new ContourConverter(geometryFactory, this));
    addConverter(SaifConstants.POINT, new PointConverter(geometryFactory));
    addConverter(SaifConstants.ALIGNED_POINT, new AlignedPointConverter(geometryFactory));
    addConverter(SaifConstants.TEXT_LINE, new TextLineConverter(geometryFactory, this));
    addConverter(SaifConstants.TEXT_ON_CURVE, new TextOnCurveConverter(geometryFactory, this));
  }
}
