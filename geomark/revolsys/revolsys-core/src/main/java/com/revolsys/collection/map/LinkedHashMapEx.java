package com.revolsys.collection.map;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapEx extends LinkedHashMap<String, Object> implements MapEx {
  private static final long serialVersionUID = 1L;

  public LinkedHashMapEx() {
    super();
  }

  public LinkedHashMapEx(final int initialCapacity) {
    super(initialCapacity);
  }

  public LinkedHashMapEx(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public LinkedHashMapEx(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public LinkedHashMapEx(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public MapEx clone() {
    return (MapEx)super.clone();
  }
}
