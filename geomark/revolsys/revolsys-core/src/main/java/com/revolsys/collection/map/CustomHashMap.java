package com.revolsys.collection.map;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.set.MapBackedSet;

import com.revolsys.util.HashEquals;

public class CustomHashMap<K, V> extends HashedMap<K, V> {
  private static final long serialVersionUID = 1L;

  public static <T> Set<T> set(final HashEquals hashEquals) {
    final CustomHashMap<T, Void> map = new CustomHashMap<>(hashEquals);
    return MapBackedSet.mapBackedSet(map);
  }

  private final HashEquals hashEquals;

  public CustomHashMap(final HashEquals hashEquals) {
    this.hashEquals = hashEquals;
  }

  public CustomHashMap(final HashEquals hashEquals, final int initialCapacity) {
    super(initialCapacity);
    this.hashEquals = hashEquals;
  }

  public CustomHashMap(final HashEquals hashEquals, final int initialCapacity,
    final float loadFactor) {
    super(initialCapacity, loadFactor);
    this.hashEquals = hashEquals;
  }

  public CustomHashMap(final HashEquals hashEquals, final Map<? extends K, ? extends V> map) {
    super(map);
    this.hashEquals = hashEquals;
  }

  @Override
  protected int hash(final Object key) {
    return this.hashEquals.hashCode(key);
  }

  @Override
  protected boolean isEqualKey(final Object key1, final Object key2) {
    return this.hashEquals.equals(key1, key2);
  }
}
