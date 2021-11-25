package com.revolsys.value;

import com.revolsys.io.BaseCloseable;

public class ValueCloseable<T> implements BaseCloseable {
  private final ValueHolder<T> valueHolder;

  private final T originalValue;

  public ValueCloseable(final ValueHolder<T> valueHolder, final T newValue) {
    this.valueHolder = valueHolder;
    this.originalValue = valueHolder.setValue(newValue);
  }

  @Override
  public void close() {
    this.valueHolder.setValue(this.originalValue);
  }

  @Override
  public String toString() {
    return this.valueHolder.toString();
  }
}
