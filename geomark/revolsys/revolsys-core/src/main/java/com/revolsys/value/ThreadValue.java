package com.revolsys.value;

import com.revolsys.io.BaseCloseable;

public final class ThreadValue<V> {
  private final ThreadLocal<V> threadValue = new ThreadLocal<>();

  private final BaseCloseable closeNull = () -> {
    this.threadValue.set(null);
  };

  private final BaseCloseable closeNoOp = () -> {
    this.threadValue.set(null);
  };

  public BaseCloseable closeable(final V value) {
    final V oldValue = setValue(value);
    if (oldValue == null) {
      return this.closeNull;
    } else if (oldValue == value) {
      return this.closeNoOp;
    } else {
      return () -> this.threadValue.set(oldValue);
    }
  }

  public V getValue() {
    final V value = this.threadValue.get();
    return value;
  }

  public boolean hasValue() {
    return this.threadValue.get() != null;
  }

  public V setValue(final V value) {
    final V oldValue = getValue();
    this.threadValue.set(value);
    return oldValue;
  }

  @Override
  public String toString() {
    final V value = getValue();
    return value.toString();
  }
}
