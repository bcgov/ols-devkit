package com.revolsys.collection.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.revolsys.util.BaseCloneable;

public class DelegatingMap<K, V> implements Map<K, V>, Cloneable {

  public static <K2, V2> Map<K2, V2> newMap(final Map<K2, V2> map) {
    return new DelegatingMap<>(map);
  }

  private Map<K, V> map;

  public DelegatingMap() {
    this(new LinkedHashMap<K, V>());
  }

  public DelegatingMap(final Map<K, V> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map cannot be null");
    }
    this.map = map;
  }

  @Override
  public void clear() {
    getMap().clear();
  }

  @Override
  protected DelegatingMap<K, V> clone() {
    try {
      @SuppressWarnings("unchecked")
      final DelegatingMap<K, V> clone = (DelegatingMap<K, V>)super.clone();
      clone.map = BaseCloneable.clone(getMap());
      return clone;
    } catch (final CloneNotSupportedException e) {
      return this;
    }
  }

  @Override
  public boolean containsKey(final Object key) {
    return getMap().containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return getMap().containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return getMap().entrySet();
  }

  @Override
  public boolean equals(final Object o) {
    return getMap().equals(o);
  }

  @Override
  public V get(final Object key) {
    return getMap().get(key);
  }

  public Map<K, V> getMap() {
    return this.map;
  }

  @Override
  public int hashCode() {
    return getMap().hashCode();
  }

  @Override
  public boolean isEmpty() {
    return getMap().isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return getMap().keySet();
  }

  @Override
  public V put(final K key, final V value) {
    return getMap().put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    getMap().putAll(m);
  }

  @Override
  public V remove(final Object key) {
    return getMap().remove(key);
  }

  public void setMap(final Map<K, V> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map cannot be null");
    }
    this.map = map;
  }

  @Override
  public int size() {
    return getMap().size();
  }

  @Override
  public String toString() {
    return getMap().toString();
  }

  @Override
  public Collection<V> values() {
    return getMap().values();
  }
}
