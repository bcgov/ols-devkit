package com.revolsys.collection.list;

import java.util.List;
import java.util.function.Supplier;

public class ThreadLocalList<T> extends AbstractDelegatingList<T> {

  private final ThreadLocal<List<T>> localList = new ThreadLocal<>();

  private Supplier<List<T>> factory;

  public ThreadLocalList() {
    this(Lists.factoryArray());
  }

  public ThreadLocalList(final Supplier<List<T>> factory) {
    this.factory = factory;
  }

  @Override
  public void clear() {
    this.localList.set(null);
  }

  @Override
  protected List<T> getList() {
    List<T> list = this.localList.get();
    if (list == null) {
      list = this.factory.get();
      this.localList.set(list);
    }
    return list;
  }
}
