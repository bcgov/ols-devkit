package com.revolsys.collection.map;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.revolsys.collection.ReferenceEntrySet;
import com.revolsys.collection.ReferenceSet;

public class WeakKeyValueMap<K, V> implements Map<K, V> {
  private final Map<K, Reference<V>> map = new WeakHashMap<>();

  public WeakKeyValueMap() {
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    return this.map.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return this.map.containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new ReferenceEntrySet<>(this.map.entrySet());
  }

  public void evict(final K key) {
    this.map.remove(key);
  }

  @Override
  public V get(final Object key) {
    V value = null;
    final Reference<V> reference = this.map.get(key);
    if (reference != null) {
      value = reference.get();
    }
    if (value == null) {
      this.map.remove(key);
    }
    return value;
  }

  @Override
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return this.map.keySet();
  }

  @Override
  public V put(final K key, final V value) {
    V oldValue = null;
    if (value == null) {
      final Reference<V> oldReference = this.map.remove(key);

      if (oldReference != null) {
        oldValue = oldReference.get();
      }
    } else {
      final Reference<V> oldReference = this.map.put(key, new WeakReference<>(value));
      if (oldReference != null) {
        oldValue = oldReference.get();
      }
    }
    return oldValue;
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    for (final Entry<? extends K, ? extends V> entry : map.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      put(key, value);
    }
  }

  @Override
  public V remove(final Object obj) {
    final Reference<V> oldReference = this.map.remove(obj);
    if (oldReference == null) {
      return null;
    } else {
      return oldReference.get();
    }
  }

  @Override
  public int size() {
    return this.map.size();
  }

  @Override
  public Collection<V> values() {
    return new ReferenceSet<>(this.map.values());
  }
}
