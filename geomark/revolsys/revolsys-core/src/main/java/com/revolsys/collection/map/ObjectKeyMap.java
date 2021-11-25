package com.revolsys.collection.map;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * <p>Wrapper around {@link Map} where the key is wrapped in an {@link ObjectKey} so that
 * == is used instead of equals for equality.</p>
 *
 * @param <K>
 * @param <V>
 */
public class ObjectKeyMap<K, V> implements Map<K, V> {
  public class Entry implements Map.Entry<K, V>, Serializable {
    private static final long serialVersionUID = -8499721149061103585L;

    private Map.Entry<ObjectKey, V> entry;

    private K key;

    private V value;

    public Entry(final K key, final V value) {
      this.key = key;
      this.value = value;
    }

    public Entry(final Map.Entry<ObjectKey, V> entry) {
      this.entry = entry;
    }

    @Override
    public boolean equals(final Object object) {
      if (object instanceof Map.Entry) {
        @SuppressWarnings("unchecked")
        final Map.Entry<K, V> entry = (Map.Entry<K, V>)object;
        if (getKey() == entry.getKey()) {
          final V value = getValue();
          final V otherValue = entry.getValue();
          if (value == null) {
            return otherValue == null;
          } else if (otherValue != null) {
            return value.equals(otherValue);
          }
        }
      }
      return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getKey() {
      if (this.entry == null) {
        return this.key;
      } else {
        return (K)this.entry.getKey().getValue();
      }
    }

    @Override
    public V getValue() {
      if (this.entry == null) {
        return this.value;
      } else {
        return this.entry.getValue();
      }
    }

    @Override
    public int hashCode() {
      return (this.key == null ? 0 : this.key.hashCode())
        ^ (this.value == null ? 0 : this.value.hashCode());
    }

    @Override
    public V setValue(final V value) {
      if (this.entry == null) {
        final V oldValue = this.value;
        this.value = value;
        return oldValue;
      } else {
        return this.entry.setValue(value);
      }
    }

    @Override
    public String toString() {
      return getKey() + "=" + getValue();
    }
  }

  private class EntrySet extends AbstractSet<java.util.Map.Entry<K, V>> {
    private final ObjectKeyMap<K, V> map;

    public EntrySet(final ObjectKeyMap<K, V> map) {
      this.map = map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(final Object object) {
      if (object instanceof Map.Entry) {
        final Map.Entry<K, V> entry = (Map.Entry<K, V>)object;
        final K key = entry.getKey();
        if (key != null) {
          final ObjectKey objectKey = new ObjectKey(key);
          final V value = entry.getValue();
          final Map.Entry<ObjectKey, V> objectKeyEntry = new AbstractMap.SimpleEntry<>(objectKey,
            value);
          return this.map.map.entrySet().contains(objectKeyEntry);
        }
      }
      return false;
    }

    @Override
    public Iterator<java.util.Map.Entry<K, V>> iterator() {
      return new EntrySetIterator(this.map.map.entrySet().iterator());
    }

    @Override
    public int size() {
      return this.map.size();
    }
  }

  private class EntrySetIterator implements Iterator<java.util.Map.Entry<K, V>> {
    private final Iterator<java.util.Map.Entry<ObjectKey, V>> iterator;

    public EntrySetIterator(final Iterator<java.util.Map.Entry<ObjectKey, V>> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    @Override
    public Map.Entry<K, V> next() {
      final Map.Entry<ObjectKey, V> entry = this.iterator.next();
      return new Entry(entry);
    }

    @Override
    public void remove() {
      this.iterator.remove();
    }
  }

  private class KeySet extends AbstractSet<K> {
    private final ObjectKeyMap<K, V> map;

    public KeySet(final ObjectKeyMap<K, V> map) {
      this.map = map;
    }

    @Override
    public boolean contains(final Object paramObject) {
      return this.map.containsKey(paramObject);
    }

    @Override
    public Iterator<K> iterator() {
      return new KeySetIterator(this.map.map.keySet().iterator());
    }

    @Override
    public int size() {
      return this.map.size();
    }
  }

  private class KeySetIterator implements Iterator<K> {
    private final Iterator<ObjectKey> iterator;

    public KeySetIterator(final Iterator<ObjectKey> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public K next() {
      return (K)this.iterator.next().getValue();
    }

    @Override
    public void remove() {
      this.iterator.remove();
    }
  }

  public static <KEY, VAL> Map<KEY, VAL> hash() {
    final Map<ObjectKey, VAL> map = new HashMap<>();
    return new ObjectKeyMap<>(map);
  }

  public static <KEY, VAL> Map<KEY, VAL> linkedHash() {
    final Map<ObjectKey, VAL> map = new LinkedHashMap<>();
    return new ObjectKeyMap<>(map);
  }

  public static <KEY, VAL> Map<KEY, VAL> weak() {
    final Map<ObjectKey, VAL> map = new WeakHashMap<>();
    return new ObjectKeyMap<>(map);
  }

  private EntrySet entrySet;

  private KeySet keySet;

  private Map<ObjectKey, V> map;

  public ObjectKeyMap() {
    this(new HashMap<ObjectKey, V>());
  }

  public ObjectKeyMap(final Map<ObjectKey, V> map) {
    this.map = map;
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    if (key == null) {
      return false;
    } else {
      final ObjectKey objectKey = new ObjectKey(key);
      return this.map.containsKey(objectKey);
    }
  }

  @Override
  public boolean containsValue(final Object value) {
    return this.map.containsValue(value);
  }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    if (this.entrySet == null) {
      this.entrySet = new EntrySet(this);
    }
    return this.entrySet;
  }

  @Override
  public V get(final Object key) {
    if (key == null) {
      return null;
    } else {
      final ObjectKey objectKey = new ObjectKey(key);
      return this.map.get(objectKey);
    }
  }

  @Override
  public int hashCode() {
    int i = 0;
    final Iterator<java.util.Map.Entry<K, V>> localIterator = entrySet().iterator();
    while (localIterator.hasNext()) {
      i += localIterator.next().hashCode();
    }
    return i;
  }

  @Override
  public boolean isEmpty() {
    return this.map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    if (this.keySet == null) {
      this.keySet = new KeySet(this);
    }
    return this.keySet;
  }

  @Override
  public V put(final K key, final V value) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    } else {
      final ObjectKey objectKey = new ObjectKey(key);
      if (value == null) {
        return this.map.remove(objectKey);
      } else {
        return this.map.put(objectKey, value);
      }
    }
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      put(key, value);
    }
  }

  @Override
  public V remove(final Object key) {
    if (key == null) {
      throw new NullPointerException("Key cannot be null");
    } else {
      final ObjectKey objectKey = new ObjectKey(key);
      return this.map.remove(objectKey);
    }
  }

  @Override
  public int size() {
    return this.map.size();
  }

  @Override
  public String toString() {
    final Iterator<Map.Entry<K, V>> localIterator = entrySet().iterator();
    if (!localIterator.hasNext()) {
      return "{}";
    }
    final StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append('{');
    while (true) {
      final Map.Entry<K, V> localEntry = localIterator.next();
      final Object localObject1 = localEntry.getKey();
      final Object localObject2 = localEntry.getValue();
      localStringBuilder.append(localObject1 == this ? "(this Map)" : localObject1);
      localStringBuilder.append('=');
      localStringBuilder.append(localObject2 == this ? "(this Map)" : localObject2);
      if (!localIterator.hasNext()) {
        return "}";
      }
      localStringBuilder.append(", ");
    }
  }

  @Override
  public Collection<V> values() {
    return this.map.values();
  }

}
