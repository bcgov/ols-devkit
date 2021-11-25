package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;

public class ReferenceEntryIterator<K, V> extends AbstractIterator<Entry<K, V>> {
  private Iterator<Entry<K, Reference<V>>> iterator;

  public ReferenceEntryIterator(final Collection<Entry<K, Reference<V>>> collection) {
    this.iterator = collection.iterator();
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    this.iterator = null;
  }

  @Override
  protected Entry<K, V> getNext() throws NoSuchElementException {
    while (this.iterator.hasNext()) {
      final Entry<K, Reference<V>> entry = this.iterator.next();
      final K key = entry.getKey();
      final Reference<V> reference = entry.getValue();
      if (reference != null) {
        final V value = reference.get();
        if (value != null) {
          return new SimpleImmutableEntry<>(key, value);
        }
      }
    }
    throw new NoSuchElementException();
  }
}
