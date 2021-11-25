package com.revolsys.collection.list;

import java.util.Collection;
import java.util.List;

public class ListBuilder<V> extends DelegatingList<V> {

  public ListBuilder(final List<V> list) {
    super(list);
  }

  public ListBuilder<V> addValue(final V value) {
    add(value);
    return this;
  }

  public ListBuilder<V> addValues(final Collection<? extends V> values) {
    addAll(values);
    return this;
  }

  public ListBuilder<V> addValues(@SuppressWarnings("unchecked") final V... values) {
    for (final V value : values) {
      add(value);
    }
    return this;
  }
}
