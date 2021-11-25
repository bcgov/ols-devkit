package com.revolsys.collection.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class CollectionMap<K, V, C extends Collection<V>> extends DelegatingMap<K, C> {

  public static <K2, V2> CollectionMap<K2, V2, List<V2>> array(final Map<K2, List<V2>> map) {
    return new CollectionMap<>(map, ArrayList::new);
  }

  public static <K2, V2> CollectionMap<K2, V2, List<V2>> hashArray() {
    final Map<K2, List<V2>> map = new HashMap<>();
    return array(map);
  }

  public static <K2, V2> CollectionMap<K2, V2, List<V2>> linkedHashArray() {
    final Map<K2, List<V2>> map = new LinkedHashMap<>();
    return array(map);
  }

  public static <K2, V2> CollectionMap<K2, V2, List<V2>> treeArray() {
    final Map<K2, List<V2>> map = new TreeMap<>();
    return array(map);
  }

  private final Supplier<C> collectionConstructor;

  public CollectionMap(final Map<K, C> map, final Supplier<C> listConstructor) {
    super(map);
    this.collectionConstructor = listConstructor;
  }

  public boolean addValue(final K key, final V value) {
    C collection = get(key);
    if (collection == null) {
      collection = this.collectionConstructor.get();
      put(key, collection);
    }
    return collection.add(value);
  }

  public C getOrEmpty(final K key) {
    final C collection = get(key);
    if (collection == null) {
      return this.collectionConstructor.get();
    } else {
      return collection;
    }
  }

  public boolean removeValue(final K key, final V value) {
    final C collection = get(key);
    if (collection == null) {
      return false;
    } else {
      return collection.remove(value);
    }
  }
}
