package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;

public class ReferenceIterator<V> extends AbstractIterator<V> {
  private Iterator<Reference<V>> iterator;

  public ReferenceIterator(final Collection<Reference<V>> collection) {
    this.iterator = collection.iterator();
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    this.iterator = null;
  }

  @Override
  protected V getNext() throws NoSuchElementException {
    while (this.iterator.hasNext()) {
      final Reference<V> reference = this.iterator.next();
      final V value = reference.get();
      if (value != null) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
