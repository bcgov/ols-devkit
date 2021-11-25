package com.revolsys.io.map;

import java.util.Iterator;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.IteratorReader;

public class IteratorMapReader extends IteratorReader<MapEx> implements MapReader {
  public IteratorMapReader(final Iterator<MapEx> iterator) {
    super(iterator);
  }
}
