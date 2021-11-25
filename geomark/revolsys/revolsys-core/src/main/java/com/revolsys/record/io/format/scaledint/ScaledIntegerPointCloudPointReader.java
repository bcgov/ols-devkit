package com.revolsys.record.io.format.scaledint;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.PointReader;
import com.revolsys.geometry.model.Point;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerPointCloudPointReader
  extends AbstractScaledIntegerPointCloudGeometryReader<Point> implements PointReader {
  public ScaledIntegerPointCloudPointReader(final Resource resource, final MapEx properties) {
    super(resource, properties);
  }
}
