package com.revolsys.io.map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;

public interface MapReader extends Reader<MapEx> {
  static boolean isReadable(final Object source) {
    return IoFactory.isAvailable(MapReaderFactory.class, source);
  }

  static MapReader newMapReader(final Object source) {
    final MapReaderFactory factory = IoFactory.factory(MapReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.newMapReader(source);
    }
  }
}
