package com.revolsys.collection;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SetQueue<E> extends AbstractQueue<E> {

  private final LinkedHashSet<E> set = new LinkedHashSet<>();

  @Override
  public Iterator<E> iterator() {
    return this.set.iterator();
  }

  @Override
  public boolean offer(final E o) {
    this.set.add(o);
    return true;
  }

  @Override
  public E peek() {
    final Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      final E value = iterator.next();
      return value;
    } else {
      return null;
    }
  }

  @Override
  public E poll() {
    final Iterator<E> iterator = iterator();
    if (iterator.hasNext()) {
      final E value = iterator.next();
      iterator.remove();
      return value;
    } else {
      return null;
    }
  }

  @Override
  public int size() {
    return this.set.size();
  }
}
