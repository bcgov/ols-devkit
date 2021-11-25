package com.revolsys.util;

public class SimpleValueWrapper<R> implements ValueWrapper<R> {

  protected R value;

  @Override
  public void close() {
    this.value = null;
  }

  @Override
  public synchronized R getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    if (this.value == null) {
      return super.toString();
    } else {
      return this.value.toString();
    }
  }
}
