package com.revolsys.collection.map;

import java.util.Comparator;
import java.util.Map;

public class MapValueComparator<K extends Comparable<K>, V extends Comparable<V>>
  implements Comparator<K> {

  private final Map<K, V> map;

  public MapValueComparator(final Map<K, V> map) {
    this.map = map;
  }

  @Override
  public int compare(final K k1, final K k2) {
    final V v1 = this.map.get(k1);
    final V v2 = this.map.get(k2);
    final int compare = v1.compareTo(v2);
    if (compare == 0) {
      return k1.compareTo(k2);
    } else {
      return compare;
    }
  }
}
