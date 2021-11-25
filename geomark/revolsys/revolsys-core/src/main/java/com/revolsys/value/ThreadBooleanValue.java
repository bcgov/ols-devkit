package com.revolsys.value;

import com.revolsys.io.BaseCloseable;

public final class ThreadBooleanValue implements BooleanValue {
  private final ThreadLocal<Boolean> threadValue = new ThreadLocal<>();

  private boolean defaultValue = true;

  private final BaseCloseable closeTrue = () -> {
    setValue(Boolean.TRUE);
  };

  private final BaseCloseable closeFalse = () -> {
    setValue(Boolean.FALSE);
  };

  public ThreadBooleanValue(final boolean defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public BaseCloseable closeable(final Boolean value) {
    if (setValue(value)) {
      return this.closeTrue;
    } else {
      return this.closeFalse;
    }
  }

  @Override
  public Boolean getValue() {
    final Boolean value = this.threadValue.get();
    if (value == null) {
      return this.defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public Boolean setValue(final Boolean value) {
    final boolean oldValue = getValue();
    final boolean booleanValue = value == Boolean.TRUE;
    if (booleanValue == this.defaultValue) {
      this.threadValue.set(null);
    } else {
      this.threadValue.set(booleanValue);
    }
    return oldValue;
  }

  @Override
  public String toString() {
    final Boolean value = getValue();
    return value.toString();
  }
}
