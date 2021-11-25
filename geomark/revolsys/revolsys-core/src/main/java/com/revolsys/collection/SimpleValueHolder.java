package com.revolsys.collection;

import com.revolsys.value.ValueHolder;

public class SimpleValueHolder<T> implements ValueHolder<T> {
  private T value;

  public SimpleValueHolder() {
  }

  public SimpleValueHolder(final T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return this.value;
  }

  @Override
  public T setValue(final T value) {
    final T oldValue = this.value;
    this.value = value;
    return oldValue;
  }

  @Override
  public String toString() {
    if (this.value == null) {
      return "null";
    } else {
      return this.value.toString();
    }
  }
}
