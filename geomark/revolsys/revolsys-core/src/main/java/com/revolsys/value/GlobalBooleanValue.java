package com.revolsys.value;

public class GlobalBooleanValue implements BooleanValue {
  private boolean value = true;

  public GlobalBooleanValue(final boolean value) {
    this.value = value;
  }

  @Override
  public synchronized Boolean getValue() {
    return this.value;
  }

  @Override
  public synchronized Boolean setValue(final Boolean value) {
    final boolean oldValue = this.value;
    this.value = value == Boolean.TRUE;
    return oldValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.value);
  }
}
