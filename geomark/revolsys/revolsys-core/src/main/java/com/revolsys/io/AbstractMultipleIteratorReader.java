package com.revolsys.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.collection.iterator.AbstractIterator;

public abstract class AbstractMultipleIteratorReader<T> extends AbstractReader<T>
  implements Iterator<T> {

  private AbstractIterator<T> iterator;

  private boolean loadNext = true;

  private boolean open;

  @Override
  @PreDestroy
  public void close() {
    if (this.iterator != null) {
      this.iterator.close();
      this.iterator = null;
    }
  }

  protected abstract AbstractIterator<T> getNextIterator();

  @Override
  public boolean hasNext() {
    if (this.loadNext) {
      if (this.iterator == null) {
        this.iterator = getNextIterator();
        if (this.iterator == null) {
          close();
          return false;
        }
      }
      while (!this.iterator.hasNext()) {
        this.iterator.close();
        this.iterator = getNextIterator();
        if (this.iterator == null) {
          return false;
        }
      }
      this.loadNext = false;
    }
    return true;
  }

  @Override
  public Iterator<T> iterator() {
    open();
    return this;
  }

  @Override
  public T next() {
    if (hasNext()) {
      final T object = this.iterator.next();
      process(object);
      this.loadNext = true;
      return object;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    if (!this.open) {
      this.open = true;
    }
  }

  protected void process(final T object) {
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }
}
