package com.revolsys.collection.map;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class WeakValueMap<K, V> extends AbstractMap<K, V> {
  private final class Entry extends SimpleEntry<K, V> {
    private static final long serialVersionUID = 1L;

    Entry(final K key, final V value) {
      super(key, value);
    }

    @Override
    public V setValue(final V value) {
      put(getKey(), value);

      return super.setValue(value);
    }
  }

  private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    final class EntryIterator implements Iterator<Map.Entry<K, V>> {
      final Iterator<Map.Entry<K, V>> setIterator;

      Map.Entry<K, V> currentEntry = null;

      EntryIterator(final Iterator<Map.Entry<K, V>> setIterator) {
        this.setIterator = setIterator;
      }

      @Override
      public boolean hasNext() {
        return this.setIterator.hasNext();
      }

      @Override
      public Map.Entry<K, V> next() {
        this.currentEntry = this.setIterator.next();

        return this.currentEntry;
      }

      @Override
      public void remove() {
        this.setIterator.remove();

        EntrySet.this.remove(this.currentEntry);
      }
    }

    @Override
    public boolean contains(final Object o) {
      if (o instanceof Map.Entry<?, ?>) {
        final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)o;

        return entry.getValue().equals(get(entry.getKey()));
      } else {
        return false;
      }
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      final Set<Map.Entry<K, V>> iterationSet = new HashSet<>();

      processQueue();

      for (final Map.Entry<K, ValueReference<K, V>> i : WeakValueMap.this.backingMap.entrySet()) {
        final K key = i.getKey();
        final V value = i.getValue().get();

        if (value != null) {
          iterationSet.add(new Entry(key, value));
        }
      }

      final Iterator<Map.Entry<K, V>> setIterator = iterationSet.iterator();

      return new EntryIterator(setIterator);
    }

    @Override
    public boolean remove(final Object o) {
      if (o instanceof Map.Entry<?, ?>) {
        final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)o;

        return WeakValueMap.this.remove(entry.getKey(), entry.getValue());
      } else {
        return false;
      }
    }

    @Override
    public int size() {
      return WeakValueMap.this.size();
    }
  }

  private static final class ValueReference<K, V> extends WeakReference<V> {
    final K key;

    ValueReference(final ReferenceQueue<V> referenceQueue, final K key, final V value) {
      super(value, referenceQueue);

      this.key = key;
    }
  }

  private final Map<K, ValueReference<K, V>> backingMap = new HashMap<>();

  private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

  private final Set<Map.Entry<K, V>> entrySet = new EntrySet();

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return this.entrySet;
  }

  @Override
  public V get(final Object key) {
    Objects.requireNonNull(key);

    processQueue();

    return resolveReference(this.backingMap.get(key));
  }

  private void processQueue() {
    while (true) {
      final ValueReference<?, ?> reference = (ValueReference<?, ?>)this.referenceQueue.poll();

      if (reference == null) {
        break;
      }

      this.backingMap.remove(reference.key);
    }
  }

  @Override
  public V put(final K key, final V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);

    processQueue();

    return resolveReference(
      this.backingMap.put(key, new ValueReference<>(this.referenceQueue, key, value)));
  }

  @Override
  public V remove(final Object key) {
    Objects.requireNonNull(key);

    processQueue();

    return resolveReference(this.backingMap.remove(key));
  }

  private V resolveReference(final Reference<V> reference) {
    if (reference == null) {
      return null;
    } else {
      return reference.get();
    }
  }

  @Override
  public int size() {
    processQueue();

    return this.backingMap.size();
  }
}
