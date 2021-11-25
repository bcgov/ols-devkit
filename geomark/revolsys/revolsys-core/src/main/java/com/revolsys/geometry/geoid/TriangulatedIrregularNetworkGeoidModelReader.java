package com.revolsys.geometry.geoid;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class TriangulatedIrregularNetworkGeoidModelReader extends BaseObjectWithProperties
  implements GeoidModelReader {

  private final Resource resource;

  public TriangulatedIrregularNetworkGeoidModelReader(final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return BoundingBox.empty();
  }

  @Override
  public GeoidModel read() {
    final TriangulatedIrregularNetwork tin = TriangulatedIrregularNetwork
      .newTriangulatedIrregularNetwork(this.resource, getProperties());
    if (tin == null) {
      return null;
    } else {
      final String geoidName = getProperty("geoidName", this.resource.getBaseName());
      return new TriangulatedIrregularNetworkGeoidModel(geoidName, tin);
    }
  }
}
