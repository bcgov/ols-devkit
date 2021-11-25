package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.io.Reader;
import com.revolsys.properties.BaseObjectWithProperties;

public abstract class AbstractIterator<T> extends BaseObjectWithProperties
  implements Iterator<T>, Reader<T> {

  private boolean hasNext = true;

  private boolean initialized;

  private boolean loadNext = true;

  private T object;

  @Override
  @PreDestroy
  public final void close() {
    this.hasNext = false;
    this.object = null;
    closeDo();
  }

  protected void closeDo() {
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  protected abstract T getNext() throws NoSuchElementException;

  protected T getObject() {
    return this.object;
  }

  @Override
  public synchronized final boolean hasNext() {
    if (this.hasNext) {
      init();
      if (this.loadNext) {
        try {
          this.object = getNext();
          this.loadNext = false;
        } catch (final NoSuchElementException e) {
          close();
          this.hasNext = false;
        }
      }
    }
    return this.hasNext;
  }

  public synchronized void init() {
    if (!this.initialized) {
      this.initialized = true;
      initDo();
    }
  }

  protected void initDo() {
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public synchronized final T next() {
    if (hasNext()) {
      final T currentObject = this.object;
      this.loadNext = true;
      return currentObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    init();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected void setLoadNext(final boolean loadNext) {
    this.loadNext = loadNext;
    this.hasNext = true;
  }
}
