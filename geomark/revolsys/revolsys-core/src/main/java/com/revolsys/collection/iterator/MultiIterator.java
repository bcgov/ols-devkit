package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class MultiIterator<V> extends AbstractIterator<V> {

  private Iterator<V> iterator;

  private final List<Iterator<V>> iterators = new LinkedList<>();

  @SuppressWarnings("unchecked")
  public MultiIterator(final Iterable<? extends Object> iterables) {
    for (final Object value : iterables) {
      if (value instanceof Iterator) {
        final Iterator<V> iterator = (Iterator<V>)value;
        this.iterators.add(iterator);
      } else if (value instanceof Iterable) {
        final Iterable<V> iterable = (Iterable<V>)value;
        final Iterator<V> iterator = iterable.iterator();
        this.iterators.add(iterator);
      } else {
        throw new IllegalArgumentException("List must contain Iterator or Iterable values");
      }
    }
  }

  @Override
  protected V getNext() throws NoSuchElementException {
    while (this.iterator == null || !this.iterator.hasNext()) {
      if (this.iterators.isEmpty()) {
        throw new NoSuchElementException();
      } else {
        this.iterator = this.iterators.remove(0);
      }
    }
    return this.iterator.next();
  }

}
