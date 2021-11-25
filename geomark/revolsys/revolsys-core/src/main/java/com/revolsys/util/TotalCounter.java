package com.revolsys.util;

import java.util.ArrayList;
import java.util.List;

public class TotalCounter implements Counter {

  private final String name;

  private List<Counter> counters = new ArrayList<>();

  public TotalCounter(final String name, final List<Counter> counters) {
    this.name = name;
    this.counters = counters;
  }

  @Override
  public long add() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long add(final long count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long add(final Number count) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long decrement() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long get() {
    long count = 0;
    for (final Counter counter : this.counters) {
      count += counter.get();
    }
    return count;
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
