package org.jeometry.common.collection.map;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LruMap<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = 1L;

  private int maxSize;

  public LruMap(final int maxSize) {
    super(maxSize, 0.75f, true);
    this.maxSize = maxSize;
  }

  @Override
  protected boolean removeEldestEntry(final Entry<K, V> eldest) {
    return size() > this.maxSize;
  }

  public void setMaxSize(final int maxSize) {
    this.maxSize = maxSize;
  }
}
