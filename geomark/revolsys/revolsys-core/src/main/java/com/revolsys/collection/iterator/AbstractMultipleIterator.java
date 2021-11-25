package com.revolsys.collection.iterator;

import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.io.FileUtil;

public abstract class AbstractMultipleIterator<T> extends AbstractIterator<T> {
  private AbstractIterator<T> iterator;

  @Override
  @PreDestroy
  public void closeDo() {
    if (this.iterator != null) {
      FileUtil.closeSilent(this.iterator);
      this.iterator = null;
    }
  }

  protected synchronized AbstractIterator<T> getIterator() {
    if (this.iterator == null) {
      this.iterator = getNextIterator();
    }
    return this.iterator;
  }

  @Override
  protected synchronized T getNext() throws NoSuchElementException {
    try {
      if (this.iterator == null) {
        this.iterator = getNextIterator();
      }
      while (this.iterator != null && !this.iterator.hasNext()) {
        FileUtil.closeSilent(this.iterator);
        this.iterator = getNextIterator();
      }
      if (this.iterator == null) {
        throw new NoSuchElementException();
      } else {
        return this.iterator.next();
      }
    } catch (final NoSuchElementException e) {
      this.iterator = null;
      throw e;
    }
  }

  /**
   * Get the next iterator, if no iterators are available throw
   * {@link NoSuchElementException}. Don't not return null.
   *
   */
  public abstract AbstractIterator<T> getNextIterator() throws NoSuchElementException;

}
