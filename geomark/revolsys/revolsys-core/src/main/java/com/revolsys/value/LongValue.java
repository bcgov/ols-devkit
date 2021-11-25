package com.revolsys.value;

public class LongValue implements ValueHolder<Long> {

  public long value;

  public long addValue(final long value) {
    this.value += value;
    return value;
  }

  @Override
  public Long getValue() {
    return this.value;
  }

  public long setValue(final long value) {
    this.value = value;
    return value;
  }

  @Override
  public Long setValue(final Long value) {
    return value;
  }
}
