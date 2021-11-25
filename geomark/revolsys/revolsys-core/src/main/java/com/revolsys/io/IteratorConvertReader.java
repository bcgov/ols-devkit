package com.revolsys.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.properties.ObjectWithProperties;

public class IteratorConvertReader<I, O> extends AbstractReader<O> implements Iterator<O> {

  private Iterator<I> iterator;

  private ObjectWithProperties object;

  private final Function<I, O> converter;

  public IteratorConvertReader(final Iterator<I> iterator, final Function<I, O> converter) {
    if (iterator == null) {
      this.iterator = Collections.emptyIterator();
    } else {
      this.iterator = iterator;
    }
    this.converter = converter;
    if (iterator instanceof ObjectWithProperties) {
      this.object = (ObjectWithProperties)iterator;
    }
  }

  @Override
  public void close() {
    try {
      if (this.iterator instanceof AbstractIterator) {
        final AbstractIterator<I> i = (AbstractIterator<I>)this.iterator;
        i.close();
      }
    } finally {
      this.iterator = Collections.emptyIterator();
    }
  }

  @Override
  public MapEx getProperties() {
    if (this.object == null) {
      return super.getProperties();
    } else {
      return this.object.getProperties();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    if (this.object == null) {
      return (C)super.getProperty(name);
    } else {
      return (C)this.object.getProperty(name);
    }
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public Iterator<O> iterator() {
    return this;
  }

  @Override
  public O next() {
    final I value = this.iterator.next();
    return this.converter.apply(value);
  }

  @Override
  public void open() {
    this.iterator.hasNext();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (this.object == null) {
      super.setProperty(name, value);
    } else {
      this.object.setProperty(name, value);
    }
  }
}
