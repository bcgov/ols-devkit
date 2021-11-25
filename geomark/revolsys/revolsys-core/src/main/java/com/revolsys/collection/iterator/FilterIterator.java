package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterIterator<T> extends AbstractIterator<T> {

  private Predicate<? super T> filter;

  private Iterator<T> iterator;

  public FilterIterator(final Predicate<? super T> filter, final Iterator<T> iterator) {
    this.filter = filter;
    this.iterator = iterator;
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    if (this.iterator instanceof AbstractIterator) {
      final AbstractIterator<T> abstractIterator = (AbstractIterator<T>)this.iterator;
      abstractIterator.close();
    }
    this.filter = null;
    this.iterator = null;
  }

  protected Predicate<? super T> getFilter() {
    return this.filter;
  }

  protected Iterator<T> getIterator() {
    return this.iterator;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    while (this.iterator != null && this.iterator.hasNext()) {
      final T value = this.iterator.next();
      if (this.filter == null || this.filter.test(value)) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
