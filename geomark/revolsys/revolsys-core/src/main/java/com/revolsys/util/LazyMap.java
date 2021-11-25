package com.revolsys.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.revolsys.collection.map.DelegatingMap;

public class LazyMap<V> extends DelegatingMap<String, V> {

  private Consumer<Map<String, V>> initializer;

  private Map<String, V> map;

  public LazyMap(final Consumer<Map<String, V>> initializer) {
    this.initializer = initializer;
  }

  @Override
  public synchronized Map<String, V> getMap() {
    if (this.initializer != null) {
      this.map = new HashMap<>();
      this.initializer.accept(this.map);
      this.initializer = null;
    }
    return this.map;
  }

}
