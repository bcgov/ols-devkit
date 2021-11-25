package com.revolsys.util;

import org.jeometry.common.data.type.DataType;

import com.revolsys.value.ValueHolder;

public final class ThreadValue<T> implements ValueHolder<T> {
  private final ThreadLocal<T> threadValue = new ThreadLocal<>();

  private T defaultValue = null;

  public ThreadValue() {
  }

  public ThreadValue(final T defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public T getValue() {
    final T value = this.threadValue.get();
    if (value == null) {
      return this.defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public T setValue(final T value) {
    final T oldValue = getValue();
    if (DataType.equal(this.defaultValue, value)) {
      this.threadValue.set(null);
    } else {
      this.threadValue.set(value);
    }
    return oldValue;
  }

  @Override
  public String toString() {
    final T value = getValue();
    return value.toString();
  }
}
