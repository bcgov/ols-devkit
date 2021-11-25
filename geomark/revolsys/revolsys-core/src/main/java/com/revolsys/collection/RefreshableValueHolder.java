package com.revolsys.collection;

import java.util.function.Supplier;

public class RefreshableValueHolder<T> extends SimpleValueHolder<T> {
  private final Supplier<T> valueSupplier;

  public RefreshableValueHolder(final Supplier<T> valueSupplier) {
    this.valueSupplier = valueSupplier;
    refresh();
  }

  public synchronized void refresh() {
    final T value = this.valueSupplier.get();
    setValue(value);
  }

}
