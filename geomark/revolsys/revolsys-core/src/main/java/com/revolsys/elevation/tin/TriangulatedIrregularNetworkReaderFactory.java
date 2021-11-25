package com.revolsys.elevation.tin;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.geoid.GeoidModelReader;
import com.revolsys.geometry.geoid.GeoidModelReaderFactory;
import com.revolsys.geometry.geoid.TriangulatedIrregularNetworkGeoidModelReader;
import com.revolsys.spring.resource.Resource;

public interface TriangulatedIrregularNetworkReaderFactory extends GeoidModelReaderFactory {
  default void forEachTriangle(final Resource resource, final MapEx properties,
    final TriangleConsumer action) {
    final TriangulatedIrregularNetwork tin = newTriangulatedIrregularNetwork(resource, properties);
    tin.forEachTriangle((triangle) -> {
      final double x1 = triangle.getX(0);
      final double y1 = triangle.getY(0);
      final double z1 = triangle.getZ(0);
      final double z2 = triangle.getZ(1);

      final double x2 = triangle.getX(1);
      final double y2 = triangle.getY(1);

      final double x3 = triangle.getX(2);
      final double y3 = triangle.getY(2);
      final double z3 = triangle.getZ(2);
      action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
    });
  }

  @Override
  default GeoidModelReader newGeoidModelReader(final Resource resource, final MapEx properties) {
    return new TriangulatedIrregularNetworkGeoidModelReader(resource, properties);
  }

  TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(Resource resource, MapEx properties);

}
