package com.revolsys.io;

import java.util.Iterator;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;

public class DelegatingReader<T> extends AbstractReader<T> {
  private Iterator<T> iterator;

  private Reader<T> reader;

  public DelegatingReader() {
  }

  public DelegatingReader(final Reader<T> reader) {
    this.reader = reader;
  }

  @Override
  public final void close() {
    try {
      if (this.reader != null) {
        this.reader.close();
      }
    } finally {
      closeDo();
    }
  }

  protected void closeDo() {
    if (this.iterator instanceof AbstractIterator) {
      final AbstractIterator<T> iter = (AbstractIterator<T>)this.iterator;
      iter.close();
    }
  }

  @Override
  public MapEx getProperties() {
    return this.reader.getProperties();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    return (C)this.reader.getProperty(name);
  }

  public Reader<T> getReader() {
    return this.reader;
  }

  @Override
  public Iterator<T> iterator() {
    if (this.iterator == null) {
      this.iterator = this.reader.iterator();
    }
    return this.iterator;
  }

  @Override
  public void open() {
    this.reader.open();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.reader.setProperty(name, value);
  }

  public void setReader(final Reader<T> reader) {
    this.reader = reader;
  }
}
