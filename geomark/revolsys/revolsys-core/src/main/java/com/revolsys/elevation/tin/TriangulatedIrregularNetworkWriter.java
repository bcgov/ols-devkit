package com.revolsys.elevation.tin;

import java.util.Collections;
import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.Resource;

public interface TriangulatedIrregularNetworkWriter extends Writer<TriangulatedIrregularNetwork> {
  static boolean isWritable(final Object source) {
    return IoFactory.isAvailable(TriangulatedIrregularNetworkWriterFactory.class, source);
  }

  static TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Object target) {
    final Map<String, ? extends Object> properties = Collections.emptyMap();
    return newTriangulatedIrregularNetworkWriter(target, properties);
  }

  static TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Object target, final Map<String, ? extends Object> properties) {
    final TriangulatedIrregularNetworkWriterFactory factory = IoFactory
      .factory(TriangulatedIrregularNetworkWriterFactory.class, target);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(target);
      final TriangulatedIrregularNetworkWriter writer = factory
        .newTriangulatedIrregularNetworkWriter(resource);
      writer.setProperties(properties);
      return writer;
    }
  }
}
