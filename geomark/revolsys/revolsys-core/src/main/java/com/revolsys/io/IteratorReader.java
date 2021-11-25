package com.revolsys.io;

import java.util.Collections;
import java.util.Iterator;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.properties.ObjectWithProperties;

public class IteratorReader<T> extends AbstractReader<T> {

  private Iterator<T> iterator;

  private ObjectWithProperties object;

  public IteratorReader() {
    setIterator(null);
  }

  public IteratorReader(final Iterator<T> iterator) {
    setIterator(iterator);
    if (iterator instanceof ObjectWithProperties) {
      this.object = (ObjectWithProperties)iterator;
    }
  }

  @Override
  public void close() {
    try {
      if (this.iterator instanceof AbstractIterator) {
        final AbstractIterator<T> i = (AbstractIterator<T>)this.iterator;
        i.close();
      }
    } finally {
      setIterator(null);
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
  public Iterator<T> iterator() {
    return this.iterator;
  }

  @Override
  public void open() {
    this.iterator.hasNext();
  }

  protected void setIterator(final Iterator<T> iterator) {
    if (iterator == null) {
      this.iterator = Collections.<T> emptyList().iterator();
    } else {
      this.iterator = iterator;
    }
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
