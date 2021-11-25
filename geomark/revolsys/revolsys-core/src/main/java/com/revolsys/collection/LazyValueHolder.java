package com.revolsys.collection;

import java.util.function.Supplier;

public class LazyValueHolder<T> extends SimpleValueHolder<T> {
  private Supplier<T> valueSupplier;

  protected LazyValueHolder() {
  }

  public LazyValueHolder(final Supplier<T> valueSupplier) {
    setValueSupplier(valueSupplier);
  }

  @Override
  public T getValue() {
    synchronized (this) {
      if (this.valueSupplier != null) {
        final T value = this.valueSupplier.get();
        super.setValue(value);
        this.valueSupplier = null;
      }
    }
    return super.getValue();
  }

  public boolean isInitialized() {
    return this.valueSupplier == null;
  }

  @Override
  public T setValue(final T value) {
    throw new UnsupportedOperationException("Value cannot be changed");
  }

  protected void setValueSupplier(final Supplier<T> valueSupplier) {
    this.valueSupplier = valueSupplier;
  }
}
