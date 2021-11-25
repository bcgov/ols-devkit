package com.revolsys.collection.map;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.collection.NameProxy;

public class NamedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements NameProxy {
  private static final long serialVersionUID = -874346734708399858L;

  private final String name;

  public NamedLinkedHashMap(final String name) {
    this.name = name;
  }

  public NamedLinkedHashMap(final String name, final K key, final V value) {
    this.name = name;
    put(key, value);
  }

  public NamedLinkedHashMap(final String name, final Map<K, V> map) {
    super(map);
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
