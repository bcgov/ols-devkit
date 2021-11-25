package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class ReferenceSet<V> extends AbstractSet<V> {

  private final Collection<Reference<V>> collection;

  public ReferenceSet(final Collection<Reference<V>> collection) {
    this.collection = collection;
  }

  @Override
  public Iterator<V> iterator() {
    return new ReferenceIterator<>(this.collection);
  }

  @Override
  public int size() {
    return this.collection.size();
  }

}
