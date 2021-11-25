package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.GeometryFactory;

public class EsriGdbGeographicCoordinateSystem extends SpatialReference {

  public EsriGdbGeographicCoordinateSystem() {
  }

  public EsriGdbGeographicCoordinateSystem(final GeometryFactory geometryFactory,
    final String wkt) {
    super(geometryFactory, wkt);
  }

}
