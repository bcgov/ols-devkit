package com.revolsys.collection.map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MapMap<K1, K2, V, M extends Map<K2, V>> extends DelegatingMap<K1, M> {

  public static <NK1, NK2, NV> MapMap<NK1, NK2, NV, Map<NK2, NV>> hash() {
    final Map<NK1, Map<NK2, NV>> map = new HashMap<>();
    return hash(map);
  }

  public static <NK1, NK2, NV> MapMap<NK1, NK2, NV, Map<NK2, NV>> hash(
    final Map<NK1, Map<NK2, NV>> map) {
    return new MapMap<>(map, HashMap::new);
  }

  public static <NK1, NK2, NV> MapMap<NK1, NK2, NV, Map<NK2, NV>> linkedHash() {
    final Map<NK1, Map<NK2, NV>> map = new LinkedHashMap<>();
    return hash(map);
  }

  public static <NK1, NK2, NV, M2 extends Map<NK2, NV>> MapMap<NK1, NK2, NV, M2> newMap(
    final Map<NK1, M2> map, final Supplier<M2> subMapConstructor) {
    return new MapMap<>(map, subMapConstructor);
  }

  public static <NK1, NK2, NV, M2 extends Map<NK2, NV>> MapMap<NK1, NK2, NV, M2> newMap(
    final Supplier<Map<NK1, M2>> mapConstructor, final Supplier<M2> subMapConstructor) {
    final Map<NK1, M2> map = mapConstructor.get();
    return newMap(map, subMapConstructor);
  }

  public static <NK1, NK2, NV> MapMap<NK1, NK2, NV, Map<NK2, NV>> tree() {
    final Map<NK1, Map<NK2, NV>> map = new TreeMap<>();
    return hash(map);
  }

  private final Supplier<M> mapConstructor;

  public MapMap(final Map<K1, M> map, final Supplier<M> mapConstructor) {
    super(map);
    this.mapConstructor = mapConstructor;
  }

  public V addValue(final K1 key, final K2 key2, final V value) {
    final M map = getMap(key);
    return map.put(key2, value);
  }

  public M getMap(final K1 key) {
    M map = get(key);
    if (map == null) {
      map = this.mapConstructor.get();
      put(key, map);
    }
    return map;
  }

  public M getOrEmpty(final K1 key) {
    final M collection = get(key);
    if (collection == null) {
      return this.mapConstructor.get();
    } else {
      return collection;
    }
  }

  public V removeKey(final K1 key, final K2 key2) {
    final M map = get(key);
    if (map == null) {
      return null;
    } else {
      return map.remove(key2);
    }
  }
}
