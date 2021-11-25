package com.revolsys.collection.list;

import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;

public class ListByIndexIterator<V> extends AbstractIterator<V> {
  private final List<V> list;

  private int index = 0;

  public ListByIndexIterator(final List<V> list) {
    this.list = list;
  }

  @Override
  protected V getNext() throws NoSuchElementException {
    if (this.index < this.list.size()) {
      try {
        return this.list.get(this.index++);
      } catch (final ArrayIndexOutOfBoundsException e) {
        throw new NoSuchElementException();
      }
    }
    throw new NoSuchElementException();
  }

  public <V2> int indexOf(final List<V2> list, final Object value) {
    int index = 0;
    try (
      ListByIndexIterator<V2> iterable = new ListByIndexIterator<>(list)) {
      if (value == null) {
        for (final V2 currentValue : iterable) {
          if (currentValue == null) {
            return index;
          } else {
            index++;
          }
        }
      } else {
        for (final V2 currentValue : iterable) {
          if (value.equals(currentValue)) {
            return index;
          } else {
            index++;
          }
        }
      }
    }
    return -1;
  }

}
