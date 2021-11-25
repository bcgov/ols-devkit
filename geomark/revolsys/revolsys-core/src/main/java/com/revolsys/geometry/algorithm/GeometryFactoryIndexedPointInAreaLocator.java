package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Location;

public class GeometryFactoryIndexedPointInAreaLocator extends IndexedPointInAreaLocator {

  public GeometryFactoryIndexedPointInAreaLocator(final Geometry geometry) {
    super(geometry);
  }

  @Override
  public Location locate(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double resolutionY = geometryFactory.getResolutionY();
    final double minY = y - resolutionY;
    final double maxY = y + resolutionY;
    final PointInArea visitor = new PointInArea(geometryFactory, x, y);
    getIndex().query(minY, maxY, visitor);

    return visitor.getLocation();
  }

}
