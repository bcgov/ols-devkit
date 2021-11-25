package com.revolsys.collection.map;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapKeySetEntrySet<K, V> extends AbstractSet<Entry<K, V>> {
  private final Map<K, V> map;

  public MapKeySetEntrySet(final Map<K, V> map) {
    this.map = map;
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  /**
   * @see java.util.AbstractCollection#contains(java.lang.Object)
   */
  @Override
  public boolean contains(final Object o) {
    if (o instanceof MapKeyEntry) {
      @SuppressWarnings("unchecked")
      final Entry<K, V> e = (Entry<K, V>)o;
      final Entry<K, V> candidate = new MapKeyEntry<>(this.map, e.getKey());
      return candidate != null && candidate.equals(e);
    } else {
      return false;
    }
  }

  @Override
  public Iterator<Map.Entry<K, V>> iterator() {
    return new MapKeySetEntryIterator<>(this.map);
  }

  @Override
  public boolean remove(final Object o) {
    return this.map.remove(o) != null;
  }

  @Override
  public int size() {
    return this.map.size();
  }
}
