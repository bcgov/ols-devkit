package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.GeometryFactory;

public class EsriGdbProjectedCoordinateSystem extends SpatialReference {
  public EsriGdbProjectedCoordinateSystem() {
  }

  public EsriGdbProjectedCoordinateSystem(final GeometryFactory geometryFactory, final String wkt) {
    super(geometryFactory, wkt);
  }

}
