package com.revolsys.io.map;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface MapWriter extends Writer<Map<String, ? extends Object>> {
  static boolean isWritable(final Object source) {
    return IoFactory.isAvailable(MapWriterFactory.class, source);
  }

  static MapWriter newMapWriter(final Object source) {
    final MapWriterFactory factory = IoFactory.factory(MapWriterFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.newMapWriter(source);
    }
  }
}
