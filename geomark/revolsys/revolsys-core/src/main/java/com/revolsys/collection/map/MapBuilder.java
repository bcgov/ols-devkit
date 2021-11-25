package com.revolsys.collection.map;

import java.util.Map;

public class MapBuilder<K, V> extends DelegatingMap<K, V> {
  private final Map<K, V> map;

  public MapBuilder(final Map<K, V> map) {
    this.map = map;
  }

  public MapBuilder<K, V> add(final K key, final V value) {
    this.map.put(key, value);
    return this;
  }

  public MapBuilder<K, V> addValue(final K key, final V value) {
    this.map.put(key, value);
    return this;
  }

  @Override
  public Map<K, V> getMap() {
    return this.map;
  }
}
