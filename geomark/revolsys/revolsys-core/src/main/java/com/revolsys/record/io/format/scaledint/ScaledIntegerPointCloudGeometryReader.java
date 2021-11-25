package com.revolsys.record.io.format.scaledint;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerPointCloudGeometryReader
  extends AbstractScaledIntegerPointCloudGeometryReader<Geometry> implements GeometryReader {
  public ScaledIntegerPointCloudGeometryReader(final Resource resource, final MapEx properties) {
    super(resource, properties);
  }
}
