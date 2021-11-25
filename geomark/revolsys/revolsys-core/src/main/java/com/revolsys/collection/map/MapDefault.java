
package com.revolsys.collection.map;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public interface MapDefault<K, V> extends Map<K, V> {
  @Override
  default void clear() {
    final Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
    entrySet.clear();
  }

  @Override
  default boolean containsKey(final Object key) {
    final Set<Entry<K, V>> entrySet = entrySet();
    if (key == null) {
      for (final Entry<K, V> entry : entrySet) {
        final K entryKey = entry.getKey();
        if (entryKey == null) {
          return true;
        }
      }
    } else {
      for (final Entry<K, V> entry : entrySet) {
        final K entryKey = entry.getKey();
        if (key.equals(entryKey)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  default boolean containsValue(final Object value) {
    final Set<Entry<K, V>> entrySet = entrySet();
    if (value == null) {
      for (final Entry<K, V> entry : entrySet) {
        final V entryValue = entry.getValue();
        if (entryValue == null) {
          return true;
        }
      }
    } else {
      for (final Entry<K, V> entry : entrySet) {
        final V entryValue = entry.getValue();
        if (value.equals(entryValue)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  default V get(final Object key) {
    final Set<Entry<K, V>> entrySet = entrySet();
    if (key == null) {
      for (final Entry<K, V> entry : entrySet) {
        final K entryKey = entry.getKey();
        if (entryKey == null) {
          final V entryValue = entry.getValue();
          return entryValue;
        }
      }
    } else {
      for (final Entry<K, V> entry : entrySet) {
        final K entryKey = entry.getKey();
        if (key.equals(entryKey)) {
          final V entryValue = entry.getValue();
          return entryValue;
        }
      }
    }
    return null;
  }

  @Override
  default boolean isEmpty() {
    return size() == 0;
  }

  @Override
  default Set<K> keySet() {
    return new AbstractSet<>() {
      @Override
      public void clear() {
        MapDefault.this.clear();
      }

      @Override
      public boolean contains(final Object k) {
        return MapDefault.this.containsKey(k);
      }

      @Override
      public boolean isEmpty() {
        return MapDefault.this.isEmpty();
      }

      @Override
      public Iterator<K> iterator() {
        return new Iterator<>() {
          private final Iterator<Entry<K, V>> interator = entrySet().iterator();

          @Override
          public boolean hasNext() {
            return this.interator.hasNext();
          }

          @Override
          public K next() {
            return this.interator.next().getKey();
          }

          @Override
          public void remove() {
            this.interator.remove();
          }
        };
      }

      @Override
      public int size() {
        return MapDefault.this.size();
      }
    };
  }

  default boolean mapEquals(final Map<?, ?> map) {
    if (map == this) {
      return true;
    } else if (map.size() != size()) {
      return false;
    } else {
      try {
        final Set<Entry<K, V>> entrySet = entrySet();
        for (final Entry<K, V> entry : entrySet) {
          final K key = entry.getKey();
          final V value = entry.getValue();
          if (value == null) {
            if (!(map.get(key) == null && map.containsKey(key))) {
              return false;
            }
          } else {
            if (!value.equals(map.get(key))) {
              return false;
            }
          }
        }
      } catch (final ClassCastException unused) {
        return false;
      } catch (final NullPointerException unused) {
        return false;
      }
      return true;
    }
  }

  default int mapHashCode() {
    int hash = 0;
    final Set<Entry<K, V>> entrySet = entrySet();
    for (final Entry<K, V> entry : entrySet) {
      hash += entry.hashCode();
    }
    return hash;
  }

  default String mapToString() {
    final Set<java.util.Map.Entry<K, V>> entrySet = entrySet();
    final Iterator<Entry<K, V>> i = entrySet.iterator();
    if (entrySet.size() == 0) {
      return "{}";
    } else {
      final StringBuilder string = new StringBuilder();
      string.append('{');
      boolean first = true;
      for (final Entry<K, V> entry : entrySet) {
        if (first) {
          first = false;
        } else {
          string.append(',').append(' ');
        }
        final K key = entry.getKey();
        final V value = entry.getValue();
        string.append(key == this ? "(this Map)" : key);
        string.append('=');
        string.append(value == this ? "(this Map)" : value);
        if (!i.hasNext()) {
        }
        string.append(',').append(' ');
      }
      return string.append('}').toString();
    }
  }

  @Override
  default V put(final K key, final V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default void putAll(final Map<? extends K, ? extends V> values) {
    for (final Entry<? extends K, ? extends V> entry : values.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      put(key, value);
    }
  }

  @Override
  default V remove(final Object key) {
    final Set<Map.Entry<K, V>> entrySet = entrySet();
    final Iterator<Entry<K, V>> i = entrySet.iterator();
    Entry<K, V> correctEntry = null;
    if (key == null) {
      while (correctEntry == null && i.hasNext()) {
        final Entry<K, V> e = i.next();
        if (e.getKey() == null) {
          correctEntry = e;
        }
      }
    } else {
      while (correctEntry == null && i.hasNext()) {
        final Entry<K, V> e = i.next();
        if (key.equals(e.getKey())) {
          correctEntry = e;
        }
      }
    }

    V oldValue = null;
    if (correctEntry != null) {
      oldValue = correctEntry.getValue();
      i.remove();
    }
    return oldValue;
  }

  @Override
  default int size() {
    return entrySet().size();
  }

  @Override
  default Collection<V> values() {
    return new AbstractCollection<>() {
      @Override
      public void clear() {
        MapDefault.this.clear();
      }

      @Override
      public boolean contains(final Object v) {
        return MapDefault.this.containsValue(v);
      }

      @Override
      public boolean isEmpty() {
        return MapDefault.this.isEmpty();
      }

      @Override
      public Iterator<V> iterator() {
        return new Iterator<>() {
          private final Iterator<Entry<K, V>> iterator = entrySet().iterator();

          @Override
          public boolean hasNext() {
            return this.iterator.hasNext();
          }

          @Override
          public V next() {
            return this.iterator.next().getValue();
          }

          @Override
          public void remove() {
            this.iterator.remove();
          }
        };
      }

      @Override
      public int size() {
        return MapDefault.this.size();
      }
    };
  }
}
