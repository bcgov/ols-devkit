package com.revolsys.util;

import java.util.concurrent.atomic.AtomicLong;

public class LongCounter extends AtomicLong implements Counter {

  private static final long serialVersionUID = 1L;

  private final String name;

  public LongCounter(final String name) {
    this.name = name;
  }

  public LongCounter(final String name, final Number count) {
    this.name = name;
    add(count);
  }

  @Override
  public long add() {
    return incrementAndGet();
  }

  @Override
  public long add(final long count) {
    return addAndGet(count);
  }

  @Override
  public long add(final Number count) {
    return addAndGet(count.longValue());
  }

  @Override
  public long decrement() {
    return decrementAndGet();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name + "=" + get();
  }
}
