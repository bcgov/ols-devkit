package com.revolsys.util;

import java.util.function.Supplier;

public class LazyValue<V> extends SimpleValueWrapper<V> {

  public static <T> LazyValue<T> newValue(final Supplier<T> supplier) {
    return new LazyValue<>(supplier);
  }

  private boolean initialized = false;

  private Supplier<V> supplier;

  public LazyValue(final Supplier<V> supplier) {
    this.supplier = supplier;
  }

  public synchronized void clearValue() {
    this.initialized = false;
    this.value = null;
  }

  @Override
  public synchronized void close() {
    super.close();
    this.supplier = null;
  }

  @Override
  public synchronized V getValue() {
    final Supplier<V> supplier = this.supplier;
    if (!this.initialized && supplier != null) {
      this.initialized = true;
      this.value = supplier.get();
    }
    return this.value;
  }

}
